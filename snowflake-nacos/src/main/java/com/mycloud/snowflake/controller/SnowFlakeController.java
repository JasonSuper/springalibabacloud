package com.mycloud.snowflake.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.mycloud.snowflake.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/snowflake")
public class SnowFlakeController {

    @Autowired
    public SnowFlake snowFlake;

    @PostConstruct
    private static void initFlowRule() {
        List<FlowRule> rules = new ArrayList<>();
        // 流控规则
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("snowflake"); // 设置受保护资源
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
        degradeRule.setResource("snowflake"); // 设置受保护资源
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

    @SentinelResource(value = "snowflake", fallback = "fallbackForGetSnowFlakeId", blockHandler = "blockHandlerForGetSnowFlakeId")
    @GetMapping("/getSnowFlakeId")
    public Map<String, Object> getSnowFlakeId() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("id", snowFlake.nextId());
        return map;
    }

    public Map<String, Object> fallbackForGetSnowFlakeId(Throwable ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("id", null);
        map.put("msg", "id服务异常，可能是时钟回拨");
        return map;
    }

    public Map<String, Object> blockHandlerForGetSnowFlakeId(BlockException ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("id", null);
        map.put("msg", "id服务被限流或熔断降级了");
        return map;
    }
}
