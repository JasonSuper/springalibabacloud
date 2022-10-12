package com.mycloud.ordernacos.service.fallback;

import com.mycloud.ordernacos.service.StockFeignService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class StockFeignFallBack implements StockFeignService {

    @Override
    public String reduce(@RequestParam("num") String num) {
        return "Feign降级了";
    }

    @Override
    public String reduce2() {
        return "Feign降级了";
    }

    @Override
    public String test() {
        return "Feign降级了";
    }
}
