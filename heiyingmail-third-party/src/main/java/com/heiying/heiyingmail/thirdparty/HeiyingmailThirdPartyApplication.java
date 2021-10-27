package com.heiying.heiyingmail.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class HeiyingmailThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeiyingmailThirdPartyApplication.class, args);
    }

}
