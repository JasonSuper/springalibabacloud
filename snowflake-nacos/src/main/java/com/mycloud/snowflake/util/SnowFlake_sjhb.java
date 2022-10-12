package com.mycloud.snowflake.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * twitter的snowflake算法 -- java实现
 * 增加时间回拨处理
 *
 * @author beyond
 * @date 2016/11/26
 */
@Component
public class SnowFlake_sjhb {

    /**
     * 起始的时间戳
     * 2022-04-01，根据实际状况更改
     */
    private final static long START_STMP = 1648742400000L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    /**
     * AtomicLongArray 环的大小，可保存1000毫秒内，每个毫秒数上一次的MessageId，时间回退的时候依赖与此
     */
    private static final int CAPACITY = 1000;
    /**
     * messageId环，解决时间回退的关键，亦可在多线程情况下减少毫秒数切换的竞争
     */
    private AtomicLongArray messageIdCycle = new AtomicLongArray(CAPACITY);

    @Value("${datacenterId}")
    private long datacenterId;  //数据中心
    @Value("${machineId}")
    private long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    public SnowFlake_sjhb() {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
    }

    public SnowFlake_sjhb(int datacenterId, int machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    public long nextId() {
        do {
            long currStmp = getNewstmp();
            long timestamp = (currStmp - START_STMP);

            // 获取当前时间在messageIdCycle 中的下标，用于获取环中上一个MessageId
            int index = (int) (timestamp % CAPACITY);
            long messageIdInCycle = messageIdCycle.get(index);

            //通过在messageIdCycle 获取到的messageIdInCycle，计算上一个MessageId的时间戳
            long timestampInCycle = messageIdInCycle >> TIMESTMP_LEFT;

            // 如果timestampInCycle 并没有设置时间戳，或时间戳小于当前时间，认为需要设置新的时间戳
            if (messageIdInCycle == 0 || timestampInCycle < timestamp) {
                long messageId = messageid(timestamp);

                // 使用CAS的方式保证在该条件下，messageId 不被重复
                if (messageIdCycle.compareAndSet(index, messageIdInCycle, messageId)) {
                    return messageId;
                }
                System.out.println("messageId cycle CAS1 failed");
            }

            // 如果当前时间戳与messageIdCycle的时间戳相等，使用环中的序列号+1的方式，生成新的序列号
            // 如果发生了时间回退的情况，（即timestampInCycle > timestamp的情况）那么不能也更新messageIdCycle 的时间戳，使用Cycle中MessageId+1
            if (timestampInCycle >= timestamp) {
                long sequence = messageIdInCycle & MAX_SEQUENCE;
                if (sequence >= MAX_SEQUENCE) {
                    System.out.println("over sequence mask :" + sequence);
                    continue;
                }
                long messageId = messageIdInCycle + 1L;
                // 使用CAS的方式保证在该条件下，messageId 不被重复
                if (messageIdCycle.compareAndSet(index, messageIdInCycle, messageId)) {
                    return messageId;
                }
                System.out.println("messageId cycle CAS2 failed");
            }
        } while (true);
    }

    private long messageid(long stmp) {
        return stmp << TIMESTMP_LEFT //时间戳部分 左移22位
                | datacenterId << DATACENTER_LEFT       //数据中心部分 左移17位
                | machineId << MACHINE_LEFT             //机器标识部分 左移12位
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake_sjhb snowFlake = new SnowFlake_sjhb(2, 3);

        for (int i = 0; i < (1 << 12); i++) {
            System.out.println(snowFlake.nextId());
        }

    }
}