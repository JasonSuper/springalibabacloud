package com.mycloud.ordernacos.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.context.annotation.Bean;

/**
 * 若您是通过 Spring Cloud Alibaba 接入的 Sentinel，则无需额外进行配置即可使用 @SentinelResource 注解。
 */
//@Configuration
public class SentinelAspectConfiguration {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}