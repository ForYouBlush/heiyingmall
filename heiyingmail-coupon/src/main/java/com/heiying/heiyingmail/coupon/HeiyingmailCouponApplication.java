package com.heiying.heiyingmail.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.heiying.heiyingmail.coupon.dao")
@EnableDiscoveryClient
public class HeiyingmailCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailCouponApplication.class, args);
    }

}
