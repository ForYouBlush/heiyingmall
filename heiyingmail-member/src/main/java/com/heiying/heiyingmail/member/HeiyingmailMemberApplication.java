package com.heiying.heiyingmail.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.heiying.heiyingmail.member.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.heiying.heiyingmail.member.fegin")
@EnableRedisHttpSession
public class HeiyingmailMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailMemberApplication.class, args);
    }

}
