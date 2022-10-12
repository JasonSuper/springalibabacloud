package com.mycloud.stocknacos.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.mycloud.stocknacos.service.StockService;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/stock")
public class StockController {

    Logger logger = LoggerFactory.getLogger(StockController.class);

    @Value("${server.port}")
    private String port;

    @Resource
    private StockService stockService;

    @PostConstruct
    private static void initFlowRule() {
        List<FlowRule> rules = new ArrayList<>();
        // 流控规则
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("reduce"); // 设置受保护资源
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 设置流控规则QPS
        // 设置受保护的资源阈值
        // Set limit QPS to 20
        flowRule.setCount(1);
        rules.add(flowRule);

        // 加载配置的规则
        FlowRuleManager.loadRules(rules);
    }

    /**
     * 熔断时长过后，会进入半开状态，如果第一次请求异常，则会再次熔断，无视配置规则，除非再次进入全开状态
     */
    @PostConstruct
    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        // 降级规则
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("degrade"); // 设置受保护资源
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT); // 设置降级规则异常数
        // 触发熔断的异常数
        degradeRule.setCount(2);
        // 触发熔断最少请求数
        degradeRule.setMinRequestAmount(2);
        // 统计时长：单位ms；结合上面：一分钟内执行了2次，出现2次异常，就会触发熔断
        degradeRule.setStatIntervalMs(60 * 1000);
        // 熔断持续时长
        degradeRule.setTimeWindow(10);
        rules.add(degradeRule);

        // 加载配置的规则
        DegradeRuleManager.loadRules(rules);
    }

    //@SentinelResource(value = "test", fallback = "fallbackForStockTotal", blockHandler = "blockHandlerForStockTotal")
    @RequestMapping("/test")
    public String test(String num) {
        return "正常访问";
    }

    @Trace
    @Tags({@Tag(key = "param", value = "arg[0]"), @Tag(key = "result", value = "returnedObj")})
    @RequestMapping("/reduce")
    public String reduce(String num) {
        Entry entry = null;
        try {
            entry = SphU.entry("reduce");
            System.out.println("扣减库存成功！" + num);
            return "扣减库存成功！" + port;
        } catch (BlockException be) {
            logger.info("被流控了");
            return "被流控了";
        } catch (Exception e) {
            Tracer.traceEntry(e, entry);
        } finally {
            if(entry != null) {
                entry.exit();
            }
        }
        return null;
    }

    @RequestMapping("/reduce2")
    public String reduce2() {
        int i = 1 / 0;
        return "正常访问";
    }

    // 原本的业务方法.
    // blockHandler优先级比fallback高
    // blockHandlerClass、fallbackClass不同类的方法必须是static修饰
    @SentinelResource(value = "reduce", fallback = "fallbackForStockTotal", blockHandler = "blockHandlerForStockTotal")
    @RequestMapping("/stockTotal")
    public String stockTotal(String id) {
        throw new RuntimeException("getStockTotal command failed");
    }

    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public String blockHandlerForStockTotal(String id, BlockException ex) {
        return "被流控了";
    }

    // fallback 函数，原方法调用抛出异常的时候调用
    public String fallbackForStockTotal(String id, Throwable ex) {
        return "报错了";
    }


    @SentinelResource(value = "degrade", blockHandler = "blockHandlerForDegrade", entryType = EntryType.IN)
    @RequestMapping("/degrade")
    public String degrade() {
        throw new RuntimeException("getStockTotal command failed");
    }

    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public String blockHandlerForDegrade(BlockException ex) {
        return "被熔断降级了";
    }

    @RequestMapping("/u1")
    public String u1() {
        return stockService.getUser();
    }

    @RequestMapping("/u2")
    public String u2() {
        return stockService.getUser();
    }
}
