package com.hc.rpc.registry;

import com.hc.rpc.common.ProviderMeta;

import java.util.List;

/**
 * @Author hc
 */
public interface IRegistry {

    // --------- 服务端 ---------

    // 服务注册
    void register(ProviderMeta providerMeta) throws Exception;
    // 服务注销
    void unRegister(ProviderMeta providerMeta) throws Exception;
    // 心跳检测
    void heartbeat();
    // 服务销毁
    void destroy();


    // --------- 客户端 ---------

    // 获取providerName的所有实例信息
    List<ProviderMeta> discoveries(String providerName);
    // 监听
    void watch(String serviceNodeKey);
}
