package com.heiying.heiyingmail.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableRabbit
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession
@EnableFeignClients
@MapperScan("com.heiying.heiyingmail.order.dao")
public class HeiyingmailOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailOrderApplication.class, args);
    }

}
