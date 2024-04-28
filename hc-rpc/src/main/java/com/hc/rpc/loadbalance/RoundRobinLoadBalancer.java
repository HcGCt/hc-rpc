package com.hc.rpc.loadbalance;

import com.hc.rpc.common.ProviderMate;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.registry.IRegistryCenter;
import com.hc.rpc.registry.RedisRegistryCenter;
import com.hc.rpc.registry.RegistryFactory;
import com.hc.rpc.spi.SpiLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认轮询算法
 *
 * @Author hc
 */
public class RoundRobinLoadBalancer implements ILoadBalancer {
    private Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    private static AtomicInteger roundRobinId = new AtomicInteger(0);

    @Override
    public ProviderMateRes select(Object[] args, String providerName) {

        IRegistryCenter registryCenter = null;
        try {
            registryCenter = RegistryFactory.get(RpcConfig.getInstance().getRegisterType());
        } catch (Exception e) {
            logger.error("获取注册中心错误.", e);
            throw new RuntimeException(e);
        }
        List<ProviderMate> discoveries = registryCenter.discoveries(providerName);
        int size = discoveries.size();
        roundRobinId.incrementAndGet();
        if (roundRobinId.get() == Integer.MAX_VALUE) {
            // 防止越界
            roundRobinId.set(0);
        }
        // / by zero
        ProviderMate providerMate = size > 0 ? discoveries.get(roundRobinId.get() % size) : new ProviderMate();
        return ProviderMateRes.build(providerMate, discoveries);
    }
}
