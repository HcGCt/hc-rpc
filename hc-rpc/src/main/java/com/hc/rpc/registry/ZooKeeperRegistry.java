package com.hc.rpc.registry;


import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.utils.ConcurrentHashSet;
import com.hc.rpc.utils.RpcStringUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * zookeeper注册中心
 *
 * @Author hc
 */
public class ZooKeeperRegistry implements IRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    // --------- provider ---------
    private CuratorFramework client;
    private ServiceDiscovery<ProviderMeta> serviceDiscovery;
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();    // 本机注册的key集合，用于续期
    private static final String ZK_ROOT_PATH = "/hc-rpc/zk";   // 根节点

    public ZooKeeperRegistry() {
        RpcConfig rpcConfig = RpcConfig.getInstance();
        client = CuratorFrameworkFactory.builder()
                .connectString(rpcConfig.getRegisterAddress())
                .retryPolicy(new ExponentialBackoffRetry(10000, 3))
                .build();
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ProviderMeta.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ProviderMeta.class))
                .build();
        try {
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ProviderMeta providerMeta) throws Exception {
        serviceDiscovery.registerService(buildServiceInstance(providerMeta));
        // 本地服务节点信息缓存
        String registerKey = ZK_ROOT_PATH + "/" + providerMeta.getAddress();
        localRegisterNodeKeySet.add(registerKey);
        // 监听节点状态
        // watch(registerKey);
    }

    // 构建ZK服务注册信息
    private ServiceInstance<ProviderMeta> buildServiceInstance(ProviderMeta providerMeta) {
        String address = providerMeta.getAddress();
        String providerKey = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        try {
            return ServiceInstance
                    .<ProviderMeta>builder()
                    .id(address)
                    .name(providerKey)
                    .address(address)
                    .payload(providerMeta)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unRegister(ProviderMeta providerMeta) throws Exception {
        serviceDiscovery.unregisterService(buildServiceInstance(providerMeta));
        // 移除本地缓存
        String registerKey = ZK_ROOT_PATH + "/" + providerMeta.getAddress();
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public void heartbeat() {
        // 建立临时节点，发生故障临时节点直接丢失
    }

    @Override
    public void destroy() {
        try {
            // 临时节点不需主动destroy？
            for (String key : localRegisterNodeKeySet) {
                client.delete().guaranteed().forPath(key);
            }
            if (client != null) {
                client.close();
                serviceDiscovery.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --------- invoker ---------
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();   // 服务缓存
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();                   // 正在监听的key集合

    @Override
    public List<ProviderMeta> discoveries(String providerName) {
        // 优先查询服务缓存
        List<ProviderMeta> providerMetasCache = registryServiceCache.readCache();
        if (providerMetasCache != null && providerMetasCache.size() != 0) return providerMetasCache;

        // 查询注册中心
        try {
            Collection<ServiceInstance<ProviderMeta>> serviceInstances = serviceDiscovery.queryForInstances(providerName);
            List<ProviderMeta> providerMetas = serviceInstances.stream().map(ServiceInstance::getPayload).collect(Collectors.toList());
            // 写入缓存
            registryServiceCache.writeCache(providerMetas);
            watch(providerMetas.get(0).getAddress());
            return providerMetas;
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }

    /**
     * 监听服务状态
     */
    @Override
    public void watch(String serviceAddress) {
        String watchKey = ZK_ROOT_PATH + "/" + serviceAddress;
        boolean add = watchingKeySet.add(watchKey);
        if (!add) return;
        CuratorCache curatorCache = CuratorCache.build(client, watchKey);
        curatorCache.start();
        curatorCache.listenable().addListener(
                CuratorCacheListener
                        .builder()
                        .forDeletes(childData -> registryServiceCache.cleanCache())
                        .forChanges(((oldNode, node) -> registryServiceCache.cleanCache()))
                        .build()
        );
    }
}
