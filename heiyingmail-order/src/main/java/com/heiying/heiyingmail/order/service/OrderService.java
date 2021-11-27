package com.heiying.heiyingmail.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.to.mq.SeckillOrderTO;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.order.entity.OrderEntity;
import com.heiying.heiyingmail.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:58:41
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderRespVO submitOrder(OrderSubmitVO vo);

    OrderEntity getOrderStatus(String orderSn);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void createSeckillOrder(SeckillOrderTO seckillOrder);
}

