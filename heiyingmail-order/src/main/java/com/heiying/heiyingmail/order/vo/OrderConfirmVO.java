package com.heiying.heiyingmail.order.vo;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//订单确认页需要的数据
public class OrderConfirmVO {
    //收货地址
    @Getter @Setter
    List<MemberAddressVO> addressVOS;

    //所有的购物项
    @Getter @Setter
    List<OrderItemVO> items;

    //发票记录...

    /**
     * 积分
     */
    @Getter @Setter
    private Integer integration;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    @Getter @Setter
    String orderToken;

    public Integer getCount(){
        int count=0;
        if (items!=null&&items.size()>0){
            for (OrderItemVO item : items) {
                count= count+item.getCount();
            }
        }
        return count;
    }

//    BigDecimal total;//订单总额

    public BigDecimal getTotal() {
        BigDecimal sum=new BigDecimal("0");
        if (items!=null&&items.size()>0){
            for (OrderItemVO item : items) {
                sum=sum.add(item.getPrice().multiply(new BigDecimal(item.getCount())));
            }
        }
        return sum;
    }

//    BigDecimal payPrice;//应付价格

    public BigDecimal getPayPrice() {
       return getTotal();
    }
}
