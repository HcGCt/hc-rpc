package com.hc.rpc.registry;

import com.hc.rpc.common.ProviderMate;

import java.io.IOException;
import java.util.List;

/**
 * @Author hc
 */
public interface IRegistryCenter {

    // 服务注册
    void register(ProviderMate providerMate) throws Exception;

    // 服务注销
    void unRegister(ProviderMate providerMate) throws Exception;

    // 获取providerName的所有实例信息
    List<ProviderMate> discoveries(String providerName);

    // 关闭
    void destroy();
}
