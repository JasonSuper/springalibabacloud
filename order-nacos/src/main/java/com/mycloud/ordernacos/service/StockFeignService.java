package com.mycloud.ordernacos.service;

import com.mycloud.ordernacos.config.FeignConfig;
import com.mycloud.ordernacos.service.fallback.StockFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// path对应所调用接口的controller的RequestMapping
@FeignClient(value = "stock-service", path = "/stock", configuration = {FeignConfig.class}, fallback = StockFeignFallBack.class)
public interface StockFeignService {

    @RequestMapping("/reduce")
        // @RequestLine("GET /reduce")
    String reduce(@RequestParam("num") String num); // @Param("num")

    @RequestMapping("/reduce2")
    String reduce2();

    @RequestMapping("/test")
    String test();
}
