package com.hc.rpc.registry;

import com.alibaba.fastjson.JSON;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.utils.RpcStringUtil;
import com.hc.rpc.utils.UUIDUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 服务实例信息注册到redis, 启动定时任务进行心跳监测, 时间为ttl
 * @Author hc
 */
public class RedisRegistry implements IRegistry {

    private JedisPool jedisPool;
    private String UUID;
    private static final int ttl = 10 * 1000;   // 默认为10s

    private Set<String> serviceMap = new HashSet<>();

    private ScheduledExecutorService heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor();

    public RedisRegistry() {
        RpcConfig rpcConfig = RpcConfig.getInstance();
        String address = rpcConfig.getRegisterAddress();
        String ip = address.split(":")[0];
        int port = Integer.parseInt(address.split(":")[1]);

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        jedisPool = new JedisPool(poolConfig, ip, port);
        this.UUID = UUIDUtils.createUUID16();
        // 开启心跳监测
        heartbeat();
    }

    /**
     * 间隔5s进行心跳检测:
     * 获取所有节点信息，检查其过期时间是否小于当前时间，如果小于则删除改服务节点信息
     */
    @Override
    public void heartbeat() {
        long delay = 5;  // 5s检测一次
        heartbeatExecutorService.scheduleWithFixedDelay(() -> {
            for (String key : serviceMap) {
                List<ProviderMeta> providerNodes = getProviders(key);
                providerNodes.removeIf(node -> {
                    long endTime = node.getEndTime();
                    if (endTime < System.currentTimeMillis()) {
                        return true;
                    }
                    // 重置过期时间
                    if (node.getUUID().equals(UUID)) {
                        node.setEndTime(node.getEndTime() + ttl / 2);
                    }
                    return false;
                });
                // 重新加载服务实例
                loadService(key, providerNodes);
            }

        }, delay, delay, TimeUnit.SECONDS);
    }

    // 获取Jedis客户端
    private Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        RpcConfig rpcConfig = RpcConfig.getInstance();
        if (rpcConfig.getRegisterPsw() != null && rpcConfig.getRegisterPsw().length() != 0) {
            jedis.auth(rpcConfig.getRegisterPsw());
        }
        return jedis;
    }

    private List<ProviderMeta> getProviders(String key) {
        Jedis jedis = getJedis();
        List<String> list = jedis.lrange(key, 0, -1);
        jedis.close();
        List<ProviderMeta> providerMetas = list.stream().map(o -> JSON.parseObject(o, ProviderMeta.class)).collect(Collectors.toList());
        return providerMetas;
    }

    private void loadService(String key, List<ProviderMeta> providerNodes) {
        String script = "redis.call('DEL', KEYS[1])\n" +
                "for i = 1, #ARGV do\n" +
                "   redis.call('RPUSH', KEYS[1], ARGV[i])\n" +
                "end \n" +
                "redis.call('EXPIRE', KEYS[1],KEYS[2])";
        List<String> keys = new ArrayList<>();
        keys.add(key);
        keys.add(String.valueOf(10));
        List<String> values = providerNodes.stream().map(JSON::toJSONString).collect(Collectors.toList());
        Jedis jedis = getJedis();
        jedis.eval(script, keys, values);
        jedis.close();
    }


    @Override
    public void register(ProviderMeta providerMeta) throws Exception {
        String key = providerMeta.getName();
        if (!serviceMap.contains(key)) {
            serviceMap.add(key);
        }
        providerMeta.setUUID(this.UUID);
        providerMeta.setEndTime(System.currentTimeMillis() + ttl);
        Jedis jedis = getJedis();
        String script = "redis.call('RPUSH', KEYS[1], ARGV[1])\n" +
                "redis.call('EXPIRE', KEYS[1], ARGV[2])";
        List<String> value = new ArrayList<>();
        value.add(JSON.toJSONString(providerMeta));
        value.add(String.valueOf(10));
        jedis.eval(script, Collections.singletonList(key), value);
        jedis.close();
    }

    // 主动注销服务
    @Override
    public void unRegister(ProviderMeta providerMeta) throws Exception {
    }

    @Override
    public List<ProviderMeta> discoveries(String providerName) {
        return getProviders(providerName);
    }

    @Override
    public void watch(String serviceNodeKey) {

    }

    @Override
    public void destroy() {
        heartbeatExecutorService.shutdown();
        jedisPool.close();
    }

}
