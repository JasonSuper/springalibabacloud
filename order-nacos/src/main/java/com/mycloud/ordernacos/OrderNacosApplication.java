package com.mycloud.ordernacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderNacosApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(OrderNacosApplication.class, args);

        // 测试配置中心的配置动态修改
        /*while (true) {
            String name = context.getEnvironment().getProperty("user.name");
            String sex = context.getEnvironment().getProperty("user.sex");
            System.out.println(name + sex);
            TimeUnit.SECONDS.sleep(1);
        }*/
    }
}
