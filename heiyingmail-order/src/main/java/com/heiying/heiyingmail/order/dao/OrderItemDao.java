package com.heiying.heiyingmail.order.dao;

import com.heiying.heiyingmail.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:58:41
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
