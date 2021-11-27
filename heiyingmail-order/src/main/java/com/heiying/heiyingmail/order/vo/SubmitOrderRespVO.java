package com.heiying.heiyingmail.order.vo;

import com.heiying.heiyingmail.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderRespVO {
    private OrderEntity order;
    private Integer code;//错误状态码，0-》成功
}
