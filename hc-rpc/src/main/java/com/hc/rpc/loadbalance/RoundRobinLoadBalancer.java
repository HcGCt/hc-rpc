package com.hc.rpc.loadbalance;

import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.registry.IRegistry;
import com.hc.rpc.registry.RegistryFactory;
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
    public ProviderMetaRes select(Object[] args, String providerName) {

        IRegistry registry = null;
        try {
            registry = RegistryFactory.get(RpcConfig.getInstance().getRegisterType());
        } catch (Exception e) {
            logger.error("获取注册中心错误.", e);
            throw new RuntimeException(e);
        }
        List<ProviderMeta> discoveries = registry.discoveries(providerName);
        int size = discoveries.size();
        roundRobinId.incrementAndGet();
        if (roundRobinId.get() == Integer.MAX_VALUE) {
            // 防止越界
            roundRobinId.set(0);
        }
        // / by zero
        ProviderMeta providerMeta = size > 0 ? discoveries.get(roundRobinId.get() % size) : new ProviderMeta();
        return ProviderMetaRes.build(providerMeta, discoveries);
    }
}
