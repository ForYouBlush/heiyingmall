package com.heiying.heiyingmail.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:58:41
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

