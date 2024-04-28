package com.hc.rpc.registry;

import com.hc.rpc.common.ProviderMate;
import com.hc.rpc.utils.RpcStringUtil;

import java.io.IOException;
import java.util.*;

/**
 * @Author hc
 */
public class LocalRegistryCenter implements IRegistryCenter {
    private Map<String, Set<ProviderMate>> registryData = new HashMap<>();


    @Override
    public void register(ProviderMate providerMate) throws Exception {
        String key = RpcStringUtil.buildProviderKey(providerMate.getName(), providerMate.getVersion());
        Set<ProviderMate> providerMates = registryData.get(key);
        if (providerMates == null) {
            providerMates = new HashSet<>();
        }
        providerMates.add(providerMate);
        registryData.put(key, providerMates);
    }

    @Override
    public void unRegister(ProviderMate providerMate) throws Exception {
        String key = RpcStringUtil.buildProviderKey(providerMate.getName(), providerMate.getVersion());
        if (registryData.containsKey(key)) {
            Set<ProviderMate> providerMates = registryData.get(key);
            Iterator<ProviderMate> iterator = providerMates.iterator();
            while (iterator.hasNext()) {
                ProviderMate next = iterator.next();
                if (next.equals(providerMate)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }

    @Override
    public List<ProviderMate> discoveries(String providerName) {
        if (registryData.containsKey(providerName)) {
            return new ArrayList<>(registryData.get(providerName));
        }
        return new ArrayList<>();
    }

    @Override
    public void destroy() {
        registryData.clear();
    }
}
