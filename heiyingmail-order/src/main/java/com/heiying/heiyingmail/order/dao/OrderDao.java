package com.heiying.heiyingmail.order.dao;

import com.heiying.heiyingmail.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:58:41
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void updateOrderStatus(@Param("outTradeNo") String outTradeNo, @Param("code") Integer code);
}
