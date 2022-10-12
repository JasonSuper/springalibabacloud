package com.mycloud.ordernacos.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mycloud.ordernacos.config.User;
import com.mycloud.ordernacos.service.SnowFlakeService;
import com.mycloud.ordernacos.service.StockFeignService;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
public class OrderController {

    /*@Resource
    private RestTemplate restTemplate;*/

    @Resource
    private StockFeignService stockFeignService;

    @Resource
    private SnowFlakeService snowFlakeService;

    @Resource
    private User user;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /*@RequestMapping("/add")
    public String add() {
        System.out.println("下单成功！");
        String msg = restTemplate.getForObject("http://stock-service/stock/reduce", String.class);
        return msg;
    }*/

    @RequestMapping("/test")
    public String test() {
        int i = 1 / 0;
        String msg = stockFeignService.test();
        return msg;
    }

    /**
     * 缓存穿透：查询一个null数据，解决：cache-null-values: true
     * 缓存击穿：大量并发进来同时查询一个正好过期的数据，解决：加锁sync=true
     * 缓存雪崩： 大量的key同时过期，解决：添加随机时间，指定过期时间 ，time-to-live: 3600000 #设置存活时间毫秒。这里因为缓存不是同一时间生成，所以可以固定存活时间
     */
    @Cacheable(value = "testCache", key = "#root.method.name.concat('-').concat(#root.args[0])", sync = true)
    @RequestMapping("/testCache")
    public String testCache(String arg) {
        System.out.println("进入了testCache，arg=" + arg);
        return arg;
    }

    @CacheEvict(value = "testCacheDel", key = "'testCache'.concat('-').concat(#root.args[0])")
    // 删除多个缓存时调用
    /*@Caching(evict = {
            @CacheEvict(value = "testCacheDel", key = "'testCache'.concat('-').concat(#root.args[0])")
            @CacheEvict(value = "testCacheDel", key = "'testCache'.concat('-').concat(#root.args[0])")
    })*/
    @RequestMapping("/testCacheDel")
    public String testCacheDel(String arg) {
        System.out.println("删除了testCache，arg=" + arg);
        return "删除" + arg;
    }


    /**
     * 缓存击穿，分布式锁解决方案
     * 缓存一致性，手写
     */
    public String update(String id, String val) {
        RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock("lockKey" + id);
        RLock lock = rReadWriteLock.writeLock();
        lock.lock(); //默认30秒，每隔1/3时间（10秒）会自动续锁
        try {
            // 模拟更新数据库
            // ......do something

            // 更新缓存
            redisTemplate.opsForValue().set("key" + id, val);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
    }



    /**
     * 缓存击穿，分布式锁解决方案
     * 缓存一致性，手写
     */
    public String read(String id) {
        RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock("lockKey" + id);
        RLock rLock = rReadWriteLock.readLock();
        rLock.lock();
        try {
            Object r = redisTemplate.opsForValue().get("key" + id);
            if (r != null) {
                return r.toString();
            }
        } finally {
            rLock.unlock();
        }


        //RLock lock = redissonClient.getLock("lockKey" + id); // 解决并发重建，每个商品，只能有一个线程重建缓存
        RLock wLock = rReadWriteLock.writeLock();
        wLock.lock();
        try {
            // 再次判断加锁之前，有没其他服务写入了缓存
            Object r = redisTemplate.opsForValue().get("key" + id);
            if (r != null) {
                return r.toString();
            }

            if (!"0".equals(id)) {
                redisTemplate.opsForValue().set("key" + id, id);
                return id;
            } else {
                redisTemplate.opsForValue().set("key" + id, "is null"); // 模拟数据库中不存在数据，也需要缓存一个类null数据，防止缓存穿透
                return "is null";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            wLock.unlock();
        }
    }


    @RequestMapping("/add")
    @Trace
    public String addfeign() {
        System.out.println("下单成功！" + user.getName() + "，订单号：" + snowFlakeService.getSnowFlakeId());
        String msg = stockFeignService.reduce("2");
        return msg;
    }

    @RequestMapping("/add2")
    public String addfeign2() {
        System.out.println("下单成功！" + user.getName() + "，订单号：" + snowFlakeService.getSnowFlakeId());
        String msg = stockFeignService.reduce2();
        return msg;
    }

    // 原本的业务方法.
    // blockHandler优先级比fallback高
    // blockHandlerClass、fallbackClass不同类的方法必须是static修饰
    @SentinelResource(value = "reduce", fallback = "fallbackForStockTotal", blockHandler = "blockHandlerForStockTotal")
    @RequestMapping("/total")
    public String stockTotal(String id) {
        throw new RuntimeException("getStockTotal command failed");
    }

    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public String blockHandlerForStockTotal(String id, BlockException ex) {
        System.out.println(ex.getClass());
        return "被流控了";
    }

    // fallback 函数，原方法调用抛出异常的时候调用
    public String fallbackForStockTotal(String id, Throwable ex) {
        return "报错了";
    }
}
