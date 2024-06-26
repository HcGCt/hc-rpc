package com.hc.rpc.loadbalance;

import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.registry.IRegistry;
import com.hc.rpc.registry.RegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希算法
 * 一致性哈希是一种哈希算法，用于【负载均衡】，对于数据水平切分的分布式系统(例如分布式存储系统)，减少了系统扩容/缩容时发生的数据迁移的问题。
 * 就是在移除或者增加一个结点时，能够尽可能小的改变已存在key的映射关系
 * <p>
 * TreeMap简单实现
 *
 * @Author hc
 */
public class ConsistentHashLoadBalancer implements ILoadBalancer {
    private Logger logger = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    // 默认虚拟节点个数为10,以尽可能保证节点在哈希环上分布均匀
    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "$";

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

        // 哈希映射,并获取最近的下一个节点
        ProviderMeta providerMeta = allocateNode(makeConsistentHashRing(discoveries), args[0].hashCode());
        return ProviderMetaRes.build(providerMeta, discoveries);
    }

    /**
     * TreeMap简单实现哈希环
     * TreeMap#ceilingEntry(K key) 返回大于等于key的value,若不存在则返回null并设置key->null
     */
    private TreeMap<Integer, ProviderMeta> makeConsistentHashRing(List<ProviderMeta> servers) {
        TreeMap<Integer, ProviderMeta> ring = new TreeMap<>();
        for (ProviderMeta instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((instance.getAddress() + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private ProviderMeta allocateNode(TreeMap<Integer, ProviderMeta> ring, int hashCode) {
        // 获取最近的下一个哈希环上节点位置
        Map.Entry<Integer, ProviderMeta> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }
}
