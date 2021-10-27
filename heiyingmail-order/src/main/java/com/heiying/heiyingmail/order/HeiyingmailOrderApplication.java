package com.heiying.heiyingmail.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.heiying.heiyingmail.order.dao")
public class HeiyingmailOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailOrderApplication.class, args);
    }

}
