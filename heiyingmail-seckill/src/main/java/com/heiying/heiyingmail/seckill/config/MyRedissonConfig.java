package com.heiying.heiyingmail.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {
    //Redis url should start with redis:// or rediss:// (for SSL connection)
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        //创建单节点Redisson客户端
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.122.168:6379");
        return Redisson.create(config);
    }
}
