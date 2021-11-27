package com.heiying.heiyingmail.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.heiying.common.to.mq.SeckillOrderTO;
import com.heiying.common.utils.R;
import com.heiying.common.vo.MemberRespVO;
import com.heiying.heiyingmail.seckill.feign.CouponFeignService;
import com.heiying.heiyingmail.seckill.feign.ProductFeignService;
import com.heiying.heiyingmail.seckill.interceptor.LoginUserInterceptor;
import com.heiying.heiyingmail.seckill.service.SeckillService;
import com.heiying.heiyingmail.seckill.to.SeckillSkuRedisTO;
import com.heiying.heiyingmail.seckill.vo.SeckillSessionWithSkus;
import com.heiying.heiyingmail.seckill.vo.SkuInfoVO;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;


    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描三天内需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            //上架商品
            List<SeckillSessionWithSkus> data = r.getData(new TypeReference<List<SeckillSessionWithSkus>>() {
            });
            if (data != null && data.size() > 0) {
                //缓存到redis
                //保存活动相关信息
                saveSessionInfos(data);
                //缓存活动的关联商品信息
                saveSessionSkuInfos(data);
            }


        }
    }

    @Override
    public List<SeckillSkuRedisTO> getCurrentSeckillSkus() {
        List<SeckillSkuRedisTO> vos = new ArrayList<>();
        //确定当前时间属于哪个秒杀场次
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                //seckill:sessions:1637920800000_1638014400000   进行分割取值
                String[] split = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
                long start = Long.parseLong(split[0]);
                long end = Long.parseLong(split[1]);
                long time = new Date().getTime();
                if (time >= start && time <= end) {
                    //获取这个秒杀场次需要的所有商品的信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(range);
                    if (list != null) {
                        List<SeckillSkuRedisTO> collect = list.stream().map(item -> {
                            SeckillSkuRedisTO redisTO = JSON.parseObject(String.valueOf(item), SeckillSkuRedisTO.class);
//                        redisTO.setRandomCode(null);  当秒杀开始就需要随机码
                            return redisTO;
                        }).collect(Collectors.toList());
                        vos.addAll(collect);

                    }
                }
            }

        }


        return vos;
    }

    @Override
    public SeckillSkuRedisTO getSkuSeckillInfo(Long skuId) {
        //找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            //正则 2_6  skuId匹配到6
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTO to = JSON.parseObject(json, SeckillSkuRedisTO.class);

                    //随机码处理
                    Long startTime = to.getStartTime();
                    Long endTime = to.getEndTime();
                    long current = new Date().getTime();
                    if (current >= startTime && current <= endTime) {

                    } else {
                        to.setRandomCode(null);
                    }

                    return to;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String code, Integer num) {
        MemberRespVO respVO = LoginUserInterceptor.loginUser.get();

        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SeckillSkuRedisTO redisTO = JSON.parseObject(s, SeckillSkuRedisTO.class);
            //校验合法性
            Long startTime = redisTO.getStartTime();
            Long endTime = redisTO.getEndTime();
            long current = new Date().getTime();
            long ttl = endTime - startTime;
            //校验时间
            if (current >= startTime && current <= endTime) {
                //校验随机码和key
                if (redisTO.getRandomCode().equals(code)) {
                    //验证数量是否合理
                    if (num <= redisTO.getSeckillCount().intValue()) {
                        //验证这个人是否买过了，幂等性处理；如果买过了就在redis中占位
                        String key = respVO.getId() + "_" + killId;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占位成功表示这个用户从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + code);
                            try {
                                boolean b = semaphore.tryAcquire(num);
                                if (b) {
                                    //秒杀成功，发送MQ消息
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTO seckillOrderTO = new SeckillOrderTO();
                                    seckillOrderTO.setOrderSn(timeId);
                                    seckillOrderTO.setSeckillPrice(redisTO.getSeckillPrice());
                                    seckillOrderTO.setMemberId(respVO.getId());
                                    seckillOrderTO.setNum(new BigDecimal(num.toString()));
                                    seckillOrderTO.setPromotionSessionId(redisTO.getPromotionSessionId());
                                    seckillOrderTO.setSkuId(redisTO.getSkuId());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTO);
                                    return timeId;
                                }
                                return null;

                            } catch (Exception e) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }

                } else {
                    return null;
                }
            } else {
                return null;
            }
        }


        return null;
    }


    private void saveSessionInfos(List<SeckillSessionWithSkus> sessions) {
        if (sessions==null||sessions.size()<=0){
            return;
        }
        sessions.stream().forEach(data -> {
            Long startTime = data.getStartTime().getTime();
            Long endTime = data.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime+"_"+data.getId();
            //缓存活动信息
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = data.getRelationSkus().stream()
                        .map(item -> {
                            return item.getPromotionSessionId() + "_" + item.getSkuId();
                        }).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }


    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> sessions) {
        sessions.stream().forEach(data -> {
            //准备hash操作
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            data.getRelationSkus().stream().forEach(seckillSkuVO -> {
                //随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                //没有这个key再去缓存
                if (!hashOps.hasKey(seckillSkuVO.getPromotionSessionId() + "_" + seckillSkuVO.getSkuId())) {
                    //缓存商品
                    SeckillSkuRedisTO redisTO = new SeckillSkuRedisTO();
                    //sku的基本信息
                    R info = productFeignService.getSkuInfo(seckillSkuVO.getSkuId());
                    if (info.getCode() == 0) {
                        SkuInfoVO infoData = info.getData("skuInfo", new TypeReference<SkuInfoVO>() {
                        });
                        redisTO.setSkuInfo(infoData);
                    }
                    //sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVO, redisTO);

                    //设置当前商品的秒杀时间信息
                    redisTO.setStartTime(data.getStartTime().getTime());
                    redisTO.setEndTime(data.getEndTime().getTime());
                    //随机码
                    redisTO.setRandomCode(token);

                    hashOps.put(seckillSkuVO.getPromotionSessionId() + "_" + seckillSkuVO.getSkuId(), JSON.toJSONString(redisTO));

                    //引入分布式的信号量->限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVO.getSeckillCount().intValue());
                }


            });
        });
    }
}
