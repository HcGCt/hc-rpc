package com.hc.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.utils.RpcStringUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @Author hc
 */
public class EtcdRegistry implements IRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EtcdRegistry.class);

    // --------- provider ---------
    private Client client;
    private KV kvClient;
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    private static final String ETCD_ROOT_PATH = "/hc-rpc/";

    public void EtcdRegistry() {
        RpcConfig rpcConfig = RpcConfig.getInstance();
        client = Client.builder()
                .endpoints(rpcConfig.getRegisterAddress())
                .connectTimeout(Duration.ofMillis(10000))
                .build();
        kvClient = client.getKVClient();
        heartbeat();
    }

    @Override
    public void register(ProviderMeta providerMeta) throws Exception {
        Lease leaseClient = client.getLeaseClient();
        long leaseId = leaseClient.grant(30).get().getID();
        String providerKey = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        String registerKey = ETCD_ROOT_PATH + providerKey;
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(providerMeta), StandardCharsets.UTF_8);
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
        // 本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ProviderMeta providerMeta) throws Exception {
        String providerKey = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        String registerKey = ETCD_ROOT_PATH + providerKey;
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public void heartbeat() {
        // 10 秒续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的 key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }
                        // 续签
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ProviderMeta providerMeta = JSONUtil.toBean(value, ProviderMeta.class);
                        register(providerMeta);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void destroy() {
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    // --------- invoker ---------
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    @Override
    public List<ProviderMeta> discoveries(String serviceKey) {
        List<ProviderMeta> providerMetasCache = registryServiceCache.readCache();
        if (providerMetasCache != null) {
            return providerMetasCache;
        }
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();

            List<ProviderMeta> providerMetas = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听 key 的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ProviderMeta.class);
                    })
                    .collect(Collectors.toList());

            registryServiceCache.writeCache(providerMetas);
            return providerMetas;
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // key 删除时触发
                        case DELETE:
                            // 清理注册服务缓存
                            registryServiceCache.cleanCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }
}
