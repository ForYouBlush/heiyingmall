package com.heiying.common.to.mq;

import lombok.Data;


@Data
public class StockLockedTO {
    private Long id;//库存工作单的id
    private StockDetailTO detail;//工作详情
}
