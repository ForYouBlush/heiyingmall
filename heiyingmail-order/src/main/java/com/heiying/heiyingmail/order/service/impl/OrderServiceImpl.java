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

        //?????????????????????request
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        MemberRespVO vo = LoginUserInterceptor.loginUser.get();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //?????????????????????????????????request
            RequestContextHolder.setRequestAttributes(attributes);
            //?????????????????????????????????
            List<MemberAddressVO> address = memberFeignService.getAddress(vo.getId());
            confirmVO.setAddressVOS(address);
            //????????????
            Integer integration = vo.getIntegration();
            confirmVO.setIntegration(integration);
        }, executor);


        CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(() -> {
            //?????????????????????????????????request
            RequestContextHolder.setRequestAttributes(attributes);
            //?????????????????????????????????????????????
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


        //????????????????????????

        //????????????
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
        //?????????????????????????????????
        String orderToken = vo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId());
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId()), orderToken);
        if (result == 1L) {
            //??????????????????
            //?????????
            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + respVO.getId());

            //?????????????????????????????????
            OrderCreateTO order = createOrder();

            //??????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //???????????????????????????
                saveOrder(order);
                //????????????
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

                //???????????????
                //???????????????????????????????????????seata???
                R r = wmsFeignService.orderLockStock(lockVO);
                if (r.getCode() == 0) {
                    //????????????
                    response.setOrder(order.getOrder());
//                    int i=10/0;
                    //?????????????????????MQ????????????
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                    return response;
                } else {
                    //????????????
                    response.setCode(3);
                    return response;
                }
            } else {
                response.setCode(2);
                return response;
            }
        } else {
            //??????????????????
            response.setCode(1);
            return response;
        }


//        if (redisToken!=null&&redisToken.equals(orderToken)){
//            //????????????
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
        //???????????????????????????
        OrderEntity byId = this.getById(entity.getId());
        if (byId.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()){
            //???????????????????????????
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            //??????stock???MQ?????????
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
     * ?????????????????????????????????????????????
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
     * ??????????????????????????????
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //??????????????????
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(infoEntity);


        //????????????????????????
        if ("TRADE_FINISHED".equals(vo.getTrade_status())||"TRADE_SUCCESS".equals(vo.getTrade_status())){
            //????????????
            String outTradeNo = vo.getOut_trade_no();
            baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
            return "success";
        }
        return "failed";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTO seckillOrder) {
        // ??????????????????
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = seckillOrder.getSeckillPrice().multiply(seckillOrder.getNum());
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //????????????
        this.save(orderEntity);

        //?????????????????????
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(seckillOrder.getOrderSn());
        orderItem.setRealAmount(totalPrice);
        orderItem.setSkuQuantity(seckillOrder.getNum().intValue());

        //???????????????spu??????
        R spuInfo = productFeignService.getSpuInfoBySkuId(seckillOrder.getSkuId());
        SpuInfoVO spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVO>() {
        });
        orderItem.setSpuId(spuInfoData.getId());
        orderItem.setSkuName(spuInfoData.getSpuName());
        orderItem.setSpuName(spuInfoData.getSpuName());
        orderItem.setSpuBrand(spuInfoData.getBrandId().toString());
        orderItem.setCategoryId(spuInfoData.getCatalogId());

        //?????????????????????
        orderItemService.save(orderItem);

    }

    /**
     * ??????????????????
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
     * ????????????
     *
     * @return
     */
    private OrderCreateTO createOrder() {
        OrderSubmitVO submitVO = confirmVOThreadLocal.get();

        //???????????????
        OrderCreateTO orderCreateTO = new OrderCreateTO();
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(submitVO, orderSn);
        orderCreateTO.setOrder(orderEntity);

        //??????????????????????????????
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        //????????????????????????????????????
        computePrice(orderEntity, orderItemEntities);
        orderCreateTO.setOrderItems(orderItemEntities);

        return orderCreateTO;
    }

    /**
     * ????????????????????????????????????
     *
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0.0");

        //?????????
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //??????????????????
        Integer integrationTotal = 0;
        Integer growthTotal = 0;


        //???????????????????????????????????????????????????????????????
        for (OrderItemEntity entity : orderItemEntities) {
            //??????????????????
            BigDecimal realAmount = entity.getRealAmount();
            total = total.add(realAmount);

            //??????????????????
            coupon = coupon.add(entity.getCouponAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            intergration = intergration.add(entity.getIntegrationAmount());
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //???????????????????????????
        orderEntity.setIntegration(integrationTotal.intValue());
        orderEntity.setGrowth(growthTotal.intValue());

        //??????????????????(0-????????????1-?????????)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * ??????????????????
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
        //??????????????????
        R fare = wmsFeignService.getFare(submitVO.getAddrId());
        FareVO fareResp = fare.getData(new TypeReference<FareVO>() {
        });
        //??????????????????
        entity.setFreightAmount(fareResp.getFare());
        //?????????????????????
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //????????????????????????
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * ???????????????????????????
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
     * ????????????????????????
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVO item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1????????????spu??????
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

        //2????????????sku??????
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());

        //??????StringUtils.collectionToDelimitedString???list???????????????String
        String skuAttrValues = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3???????????????
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        //4???????????????????????????
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);


        //??????????????????????????????.?????? - ??????????????????
        //???????????????
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //??????????????????????????????????????????
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);


        return orderItemEntity;
    }

}