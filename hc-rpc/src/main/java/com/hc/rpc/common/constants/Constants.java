package com.hc.rpc.common.constants;

/**
 * @Author hc
 */
public class Constants {

    public static final String DEFAULT_CONFIG_PREFIX = "hc.rpc";

    // 消息魔数
    public static final short MAGIC = 0x10;
    // 消息头长
    public static final int HEADER_TOTAL_LEN = 18;
    // 版本号
    public static final byte VERSION = 0x1;

    public static final String REDIS_REGISTRY = "redis";
    public static final String ZOOKEEPER_REGISTRY = "zookeeper";
    public static final String LOCAL_REGISTRY = "local";

    public static final String JSON_SERIALIZATION = "json";
    public static final String HESSIAN_SERIALIZATION = "hessian";

    // 负载均衡策略
    public static final String CONSISTENT_HASH = "consistentHash";
    public static final String ROUND_ROBIN = "roundRobin";

    // 容错机制
    public static final String Failover = "failover";
    public static final String FailFast = "failFast";
    public static final String Failsafe = "failsafe";

}
