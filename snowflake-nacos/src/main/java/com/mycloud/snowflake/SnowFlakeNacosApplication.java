package com.mycloud.snowflake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SnowFlakeNacosApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SnowFlakeNacosApplication.class, args);
    }
}
