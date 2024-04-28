package com.hc.rpc.loadbalance;

/**
 * 负载均衡策略
 *
 * @Author hc
 */
public interface ILoadBalancer {

    /**
     * 选择provider
     *
     * @param args
     * @param providerName
     * @return
     */
    ProviderMateRes select(Object[] args, String providerName);
}
