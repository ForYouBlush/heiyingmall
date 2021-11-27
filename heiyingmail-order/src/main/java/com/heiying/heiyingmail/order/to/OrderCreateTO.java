package com.heiying.heiyingmail.order.to;

import com.heiying.heiyingmail.order.entity.OrderEntity;
import com.heiying.heiyingmail.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTO {
    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;//订单计算的应付价格

    private BigDecimal fare;//运费
}
