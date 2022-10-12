package com.mycloud.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class StockApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
    }
}