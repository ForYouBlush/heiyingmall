package com.heiying.heiyingmail.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:58:41
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

