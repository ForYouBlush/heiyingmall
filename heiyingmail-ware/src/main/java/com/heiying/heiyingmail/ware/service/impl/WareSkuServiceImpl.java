package com.heiying.heiyingmail.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.exception.NoStockException;
import com.heiying.common.to.mq.OrderTO;
import com.heiying.common.to.mq.StockDetailTO;
import com.heiying.common.to.mq.StockLockedTO;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.ware.dao.WareSkuDao;
import com.heiying.heiyingmail.ware.entity.WareOrderTaskDetailEntity;
import com.heiying.heiyingmail.ware.entity.WareOrderTaskEntity;
import com.heiying.heiyingmail.ware.entity.WareSkuEntity;
import com.heiying.heiyingmail.ware.feign.OrderFeignService;
import com.heiying.heiyingmail.ware.feign.ProductFeignService;
import com.heiying.heiyingmail.ware.service.WareOrderTaskDetailService;
import com.heiying.heiyingmail.ware.service.WareOrderTaskService;
import com.heiying.heiyingmail.ware.service.WareSkuService;
import com.heiying.heiyingmail.ware.vo.OrderItemVO;
import com.heiying.heiyingmail.ware.vo.OrderVO;
import com.heiying.heiyingmail.ware.vo.SkuHasStockVO;
import com.heiying.heiyingmail.ware.vo.WareSkuLockVO;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    OrderFeignService orderFeignService;


    /**
     * ????????????
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    private void unLockedStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        baseMapper.unLockedStock(skuId, wareId, num);
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }

    /**
     * ????????????
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("ware_id", skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * ????????????
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1?????????????????????????????????????????????
        List<WareSkuEntity> skuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (skuEntities == null || skuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO ??????????????????????????????????????????????????????????????????????????????
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }

            } catch (Exception e) {

            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * ??????????????????sku
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockVO> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVO> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVO skuHasStockVO = new SkuHasStockVO();
            //????????????sku???????????????
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVO.setSkuId(skuId);
            skuHasStockVO.setHasStock(count == null ? false : count > 0);
            return skuHasStockVO;
        }).collect(Collectors.toList());
        return collect;

    }

    /**
     * ???????????????????????????
     *
     * @param lockVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVO lockVO) {
        /**
         * ??????????????????????????????
         * ?????????
         */
        WareOrderTaskEntity entity = new WareOrderTaskEntity();
        entity.setOrderSn(lockVO.getOrderSn());
        wareOrderTaskService.save(entity);


        List<OrderItemVO> locks = lockVO.getLocks();
        //?????????????????????????????????????????????
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //??????????????????????????????????????????
            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //????????????
        Boolean allLock = true;
        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //????????????
                throw new NoStockException();
            }
            for (Long wareId : wareIds) {
                Long count = baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    //??????MQ??????????????????
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null, skuId, null, stock.getNum(), entity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(detailEntity);
                    StockLockedTO stockLockedTO = new StockLockedTO();
                    stockLockedTO.setId(entity.getId());
                    StockDetailTO detail = new StockDetailTO();
                    BeanUtils.copyProperties(detailEntity, detail);
                    stockLockedTO.setDetail(detail);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTO);
                    break;
                } else {
                    //????????????????????????????????????????????????
                }
            }
            if (!skuStocked) {
                //???????????????????????????????????????
                throw new NoStockException();
            }
        }

        return true;
    }

    /**
     * ????????????
     * @param to
     */
    @Override
    public void unlockStock(StockLockedTO to) {
        StockDetailTO detail = to.getDetail();
        Long id = to.getId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detail.getId());
        if (byId != null) {
            //????????????
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVO orderVO = r.getData(new TypeReference<OrderVO>() {
                });

                if (orderVO == null || orderVO.getStatus() == 4) {
                    //?????????????????????????????????????????????????????????
                    if (byId.getLockStatus()==1){
                        //?????????????????????????????????????????????1?????????????????????????????????
                        unLockedStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
                    }
                }
            } else {
                //??????????????????
                throw new RuntimeException("????????????????????????");
                //?????????????????????????????????????????????????????????????????????
            }

        } else {
            //????????????
        }
    }

    //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    @Override
    @Transactional
    public void unlockStock(OrderTO to) {
        String orderSn = to.getOrderSn();
        WareOrderTaskEntity taskEntity=wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = taskEntity.getId();
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status",1));
        for (WareOrderTaskDetailEntity item : detailEntities) {
            unLockedStock(item.getSkuId(),item.getWareId(),item.getSkuNum(), item.getTaskId());
        }

    }


    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;

    }

}
