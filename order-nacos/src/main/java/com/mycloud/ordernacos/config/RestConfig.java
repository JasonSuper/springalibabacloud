package com.mycloud.ordernacos.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
/*@RibbonClients(value = {
        @RibbonClient(name = "stock-service", configuration = RuleConfig.class) // 调用stock-service服务时，使用MyRadomRule策略
})*/
// 改为配置文件模式了，这里注释掉
public class RestConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
