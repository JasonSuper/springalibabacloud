package com.mycloud.ordernacos.service;

import com.mycloud.ordernacos.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = "snowflake-service", path = "/snowflake", configuration = {FeignConfig.class})
public interface SnowFlakeService {

    @RequestMapping("/getSnowFlakeId")
    Map<String, Object> getSnowFlakeId();
}
