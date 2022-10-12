package com.mycloud.gateway.customFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义局部过滤器
 */
@Component
public class MyGatewayFilterFactory extends AbstractGatewayFilterFactory<MyGatewayFilterFactory.Config> {

    Logger log = LoggerFactory.getLogger(MyGatewayFilterFactory.class);

    public MyGatewayFilterFactory() {
        super(MyGatewayFilterFactory.Config.class);
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("value");
    }

    @Override
    public GatewayFilter apply(MyGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            if ("jack".equals(config.value)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        @NotEmpty
        private String value;

        public Config() {
        }

        public String getValue() {
            return value;
        }

        public MyGatewayFilterFactory.Config setValue(String value) {
            this.value = value;
            return this;
        }
    }
}
