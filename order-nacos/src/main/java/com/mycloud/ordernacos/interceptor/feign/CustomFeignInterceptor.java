package com.mycloud.ordernacos.interceptor.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class CustomFeignInterceptor implements RequestInterceptor {

    Logger logger = LoggerFactory.getLogger(CustomFeignInterceptor.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        /*requestTemplate.header("", "");*/
        requestTemplate.query("num", "99");
        logger.info("fegin拦截器");
    }
}
