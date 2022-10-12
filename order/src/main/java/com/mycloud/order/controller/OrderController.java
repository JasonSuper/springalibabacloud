package com.mycloud.order.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private RestTemplate restTemplate;

    @RequestMapping("/add")
    public String add() {
        System.out.println("下单成功！");
        String msg = restTemplate.getForObject("http://127.0.0.1:8090/stock/reduce", String.class);
        System.out.println("order:" + msg);
        return "下单成功！";
    }
}
