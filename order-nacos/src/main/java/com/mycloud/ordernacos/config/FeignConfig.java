package com.mycloud.ordernacos.config;

import com.mycloud.ordernacos.interceptor.feign.CustomFeignInterceptor;
import feign.Contract;
import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;

/**
 * 全局配置：@Configuration会作用于所有服务提供方
 * 局部配置：如果针对某服务，不要加@Configuration
 * @FeignClient根据服务的value，指定feignConfig
 */
//@Configuration
public class FeignConfig {

    /**
     * OpenFeign 日志增强
     *
     * @Configuration被注释，这里不生效
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * OpenFeign 契约模式，还原fegin原生注解
     *
     * @Configuration被注释，这里不生效
     */
    //@Bean
    public Contract feignContract() {
        return new Contract.Default();
    }

    /**
     * OpenFeign 超时设置
     *
     * @Configuration被注释，这里不生效
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(5000, 10000);
    }

    /**
     * OpenFeign 拦截器
     *
     * @Configuration被注释，这里不生效
     */
    @Bean
    public CustomFeignInterceptor customFeignInterceptor() {
        return new CustomFeignInterceptor();
    }
}
