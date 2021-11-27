package com.heiying.heiyingmail.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.heiying.heiyingmail.product.feign")
@MapperScan("com.heiying.heiyingmail.product.dao")
@SpringBootApplication
@EnableRedisHttpSession
public class HeiyingmailProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailProductApplication.class, args);
    }

}
