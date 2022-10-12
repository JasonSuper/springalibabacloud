package com.mycloud.ordernacos.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * springwebmvc接口资源限流入口在Handlerinterceptor的实现类AbstractSentinellnterceptor的preHandle方法中，对异常的处理是BlockExceptionHandler的实现类
 * @SentinelResource不会触发此统一处理，必须设置blockhander
 */
@Component
public class MyBlockException implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        System.out.println("自定义BlockException全局统一处理");
        e.printStackTrace();
    }
}
