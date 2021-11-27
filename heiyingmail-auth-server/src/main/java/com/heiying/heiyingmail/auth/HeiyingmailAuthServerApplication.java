package com.heiying.heiyingmail.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableRedisHttpSession
public class HeiyingmailAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailAuthServerApplication.class, args);
    }

}
