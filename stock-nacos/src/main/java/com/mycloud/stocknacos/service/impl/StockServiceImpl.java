package com.mycloud.stocknacos.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mycloud.stocknacos.service.StockService;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {

    /**
     * 链路流控必须添加web-context-unify: false
     * @return
     */
    @Override
    @SentinelResource(value = "getUser", blockHandler = "blockHandlerForGetUser")
    public String getUser() {
        return "Jason";
    }

    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public String blockHandlerForGetUser(BlockException ex) {
        return "getUser被流控了";
    }
}
