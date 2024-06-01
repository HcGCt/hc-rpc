package com.hc.rpc.registry;

import com.hc.rpc.common.ProviderMeta;

import java.util.List;

/**
 * 服务本地缓存(客户端)
 * 每个请求发送前有些查看服务端是否存在于缓存中
 *
 * @Author hc
 */
public class RegistryServiceCache {
    // 服务缓存
    List<ProviderMeta> serviceCache;

    public void writeCache(List<ProviderMeta> newServiceCache) {
        this.serviceCache = newServiceCache;
    }

    public List<ProviderMeta> readCache() {
        return this.serviceCache;
    }

    public void cleanCache() {
        this.serviceCache = null;
    }
}
