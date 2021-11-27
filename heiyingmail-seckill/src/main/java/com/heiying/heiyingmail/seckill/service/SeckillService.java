package com.heiying.heiyingmail.seckill.service;

import com.heiying.heiyingmail.seckill.to.SeckillSkuRedisTO;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTO> getCurrentSeckillSkus();

    SeckillSkuRedisTO getSkuSeckillInfo(Long skuId);

    String kill(String killId, String code, Integer num);
}
