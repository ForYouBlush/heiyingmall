package com.heiying.heiyingmail.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.heiying.common.to.mq.OrderTO;
import com.heiying.common.to.mq.SeckillOrderTO;
import com.heiying.common.utils.R;
import com.heiying.common.vo.MemberRespVO;
import com.heiying.heiyingmail.order.constant.OrderConstant;
import com.heiying.heiyingmail.order.entity.OrderItemEntity;
import com.heiying.heiyingmail.order.entity.PaymentInfoEntity;
import com.heiying.heiyingmail.order.enume.OrderStatusEnum;
import com.heiying.heiyingmail.order.feign.CartFeignService;
import com.heiying.heiyingmail.order.feign.MemberFeignService;
import com.heiying.heiyingmail.order.feign.ProductFeignService;
import com.heiying.heiyingmail.order.feign.WmsFeignService;
import com.heiying.heiyingmail.order.interceptor.LoginUserInterceptor;
import com.heiying.heiyingmail.order.service.OrderItemService;
import com.heiying.heiyingmail.order.service.PaymentInfoService;
import com.heiying.heiyingmail.order.to.OrderCreateTO;
import com.heiying.heiyingmail.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.order.dao.OrderDao;
import com.heiying.heiyingmail.order.entity.OrderEntity;
import com.heiying.heiyingmail.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private static ThreadLocal<OrderSubmitVO> confirmVOThreadLocal = new ThreadLocal<>();
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WmsFeignService wmsFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;






    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVO confirmVO = new OrderConfirmVO();

        //获取之间的请求request
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        MemberRespVO vo = LoginUserInterceptor.loginUser.get();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享请求request
            RequestContextHolder.setRequestAttributes(attributes);
            //远程查询所有的地址列表
            List<MemberAddressVO> address = memberFeignService.getAddress(vo.getId());
            confirmVO.setAddressVOS(address);
            //保存积分
            Integer integration = vo.getIntegration();
            confirmVO.setIntegration(integration);
        }, executor);


        CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享请求request
            RequestContextHolder.setRequestAttributes(attributes);
            //远程调用购物车所有选中的购物项
            List<OrderItemVO> items = cartFeignService.getCurrentUserItems();
            confirmVO.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVO> items = confirmVO.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkusHasStock(collect);
            if (hasStock.getCode() == 0) {
                List<SkuHasStockVO> data = hasStock.getData(new TypeReference<List<SkuHasStockVO>>() {
                });
                if (data != null) {
                    Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
                    confirmVO.setStocks(map);
                }
            }
        }, executor);


        //其他数据自动计算

        //防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + vo.getId(), token, 30, TimeUnit.MINUTES);

        confirmVO.setOrderToken(token);
        CompletableFuture.allOf(addressFuture, itemFuture).get();

        return confirmVO;

    }

    @Transactional
    @Override
    public SubmitOrderRespVO submitOrder(OrderSubmitVO vo) {
        SubmitOrderRespVO response = new SubmitOrderRespVO();
        MemberRespVO respVO = LoginUserInterceptor.loginUser.get();
        confirmVOThreadLocal.set(vo);
        response.setCode(0);
        //验证令牌【保证原子性】
        String orderToken = vo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId());
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId()), orderToken);
        if (result == 1L) {
            //令牌验证成功
            //删令牌
            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId());

            //创建订单，订单项等信息
            OrderCreateTO order = createOrder();

            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //验价成功，保存订单
                saveOrder(order);
                //库存锁定
                WareSkuLockVO lockVO = new WareSkuLockVO();
                List<OrderItemVO> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVO orderItemVO = new OrderItemVO();
                    orderItemVO.setSkuId(item.getSkuId());
                    orderItemVO.setCount(item.getSkuQuantity());
                    orderItemVO.setTitle(item.getSkuName());
                    return orderItemVO;
                }).collect(Collectors.toList());
                lockVO.setLocks(locks);
                lockVO.setOrderSn(order.getOrder().getOrderSn());

                //远程锁库存
                //为了适用高并发场景，不适用seata，
                R r = wmsFeignService.orderLockStock(lockVO);
                if (r.getCode() == 0) {
                    //锁定成功
                    response.setOrder(order.getOrder());
//                    int i=10/0;
                    //订单创建成功给MQ发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return response;
                } else {
                    //锁定失败
                    response.setCode(3);
                    return response;
                }
            } else {
                response.setCode(2);
                return response;
            }
        } else {
            //令牌验证失败
            response.setCode(1);
            return response;
        }


//        if (redisToken!=null&&redisToken.equals(orderToken)){
//            //验证成功
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId());
//        }else {
//            response.setCode(1);
//        }
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
       return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn",orderSn));

    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前订单的状态
        OrderEntity byId = this.getById(entity.getId());
        if (byId.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()){
            //超时，自动关闭订单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            //再给stock的MQ发消息
            OrderTO orderTO = new OrderTO();
            BeanUtils.copyProperties(byId,orderTO);
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTO);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        payVo.setOut_trade_no(orderSn);
        BigDecimal amount = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(amount.toString());
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        payVo.setSubject(order_sn.get(0).getSkuName());
        payVo.setBody(order_sn.get(0).getSkuAttrsVals());
        return payVo;
    }

    /**
     * 分页查询当前登录用户的所有订单
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVO respVO = LoginUserInterceptor.loginUser.get();
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                queryWrapper.eq("member_id",respVO.getId()).orderByDesc("modify_time")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(list);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(infoEntity);


        //修改订单状态信息
        if ("TRADE_FINISHED".equals(vo.getTrade_status())||"TRADE_SUCCESS".equals(vo.getTrade_status())){
            //支付成功
            String outTradeNo = vo.getOut_trade_no();
            baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
            return "success";
        }
        return "failed";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTO seckillOrder) {
        // 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = seckillOrder.getSeckillPrice().multiply(seckillOrder.getNum());
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //保存订单
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(seckillOrder.getOrderSn());
        orderItem.setRealAmount(totalPrice);
        orderItem.setSkuQuantity(seckillOrder.getNum().intValue());

        //保存商品的spu信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(seckillOrder.getSkuId());
        SpuInfoVO spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVO>() {
        });
        orderItem.setSpuId(spuInfoData.getId());
        orderItem.setSkuName(spuInfoData.getSpuName());
        orderItem.setSpuName(spuInfoData.getSpuName());
        orderItem.setSpuBrand(spuInfoData.getBrandId().toString());
        orderItem.setCategoryId(spuInfoData.getCatalogId());

        //保存订单项数据
        orderItemService.save(orderItem);

    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTO order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     *
     * @return
     */
    private OrderCreateTO createOrder() {
        OrderSubmitVO submitVO = confirmVOThreadLocal.get();

        //设置订单号
        OrderCreateTO orderCreateTO = new OrderCreateTO();
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(submitVO, orderSn);
        orderCreateTO.setOrder(orderEntity);

        //获取所有的订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        //计算价格、积分等相关信息
        computePrice(orderEntity, orderItemEntities);
        orderCreateTO.setOrderItems(orderItemEntities);

        return orderCreateTO;
    }

    /**
     * 计算价格、积分等相关信息
     *
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0.0");

        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;


        //订单的总额，就是叠加每一个订单项的总额信息
        for (OrderItemEntity entity : orderItemEntities) {
            //订单总额信息
            BigDecimal realAmount = entity.getRealAmount();
            total = total.add(realAmount);

            //优惠价格信息
            coupon = coupon.add(entity.getCouponAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            intergration = intergration.add(entity.getIntegrationAmount());
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal.intValue());
        orderEntity.setGrowth(growthTotal.intValue());

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 构建订单数据
     *
     * @param submitVO
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVO submitVO, String orderSn) {
        MemberRespVO memberRespVO = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberRespVO.getId());
        //获取地址信息
        R fare = wmsFeignService.getFare(submitVO.getAddrId());
        FareVO fareResp = fare.getData(new TypeReference<FareVO>() {
        });
        //设置运费信息
        entity.setFreightAmount(fareResp.getFare());
        //设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //设置订单相关状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * 构建所有订单项数据
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVO> currentUserItems = cartFeignService.getCurrentUserItems();
        if (currentUserItems != null && currentUserItems.size() > 0) {
            List<OrderItemEntity> collect = currentUserItems.stream().map(item -> {
                OrderItemEntity itemEntity = buildOrderItem(item);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVO item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1、商品的spu信息
        Long skuId = item.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        if (r.getCode() == 0) {
            SpuInfoVO spuInfoVO = r.getData(new TypeReference<SpuInfoVO>() {
            });
            orderItemEntity.setSpuId(spuInfoVO.getId());
            orderItemEntity.setSpuName(spuInfoVO.getSpuName());
            orderItemEntity.setSpuBrand(spuInfoVO.getBrandId().toString());
            orderItemEntity.setCategoryId(spuInfoVO.getCatalogId());

        }

        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());

        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3、积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        //4、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);


        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);


        return orderItemEntity;
    }

}