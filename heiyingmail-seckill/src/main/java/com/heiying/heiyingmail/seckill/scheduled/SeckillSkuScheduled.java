package com.heiying.heiyingmail.seckill.scheduled;

import com.heiying.heiyingmail.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 *          每天晚上3点：上架最近三天需要秒杀的商品。
 */
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock="seckill:upload:lock";


    @Scheduled(cron = "0/59 0/5 * * * ?")
    public void uploadSeckillSkuLatest3Days(){
//        System.out.println("秒杀商品开始缓存...");
        //重复上架无需处理
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
