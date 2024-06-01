package com.hc.rpc.registry;

import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.utils.RpcStringUtil;

import java.util.*;

/**
 * @Author hc
 */
public class LocalRegistry implements IRegistry {
    private Map<String, Set<ProviderMeta>> registryData = new HashMap<>();


    @Override
    public void register(ProviderMeta providerMeta) throws Exception {
        String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        Set<ProviderMeta> providerMetas = registryData.get(key);
        if (providerMetas == null) {
            providerMetas = new HashSet<>();
        }
        providerMetas.add(providerMeta);
        registryData.put(key, providerMetas);
    }

    @Override
    public void unRegister(ProviderMeta providerMeta) throws Exception {
        String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        if (registryData.containsKey(key)) {
            Set<ProviderMeta> providerMetas = registryData.get(key);
            Iterator<ProviderMeta> iterator = providerMetas.iterator();
            while (iterator.hasNext()) {
                ProviderMeta next = iterator.next();
                if (next.equals(providerMeta)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    @Override
    public void heartbeat() {

    }

    @Override
    public List<ProviderMeta> discoveries(String providerName) {
        if (registryData.containsKey(providerName)) {
            return new ArrayList<>(registryData.get(providerName));
        }
        return new ArrayList<>();
    }

    @Override
    public void watch(String serviceNodeKey) {

    }

    @Override
    public void destroy() {
        registryData.clear();
    }
}
