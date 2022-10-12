package com.mycloud.ribbon.rule;

import com.mycloud.ribbon.rule.MyCustomRule;
import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 必须写在@SpringBootApplication默认@ComponentScan外的包路径（默认是OrderNacosApplication所在的包）
 * 否则会导致该客户端下所有微服务均使用此负载均衡策略
 * 配合@RibbonClients注解使用
 *
 * 推荐使用yml模式
 */
@Configuration
public class RuleConfig {

    @Bean
    public IRule iRule() {
        return new MyCustomRule();
    }
}
