package com.hc.rpc.loadbalance;

import com.hc.rpc.spi.SpiLoader;

/**
 * @Author hc
 */
public class LoadBalancerFactory {

    static {
        SpiLoader.load(ILoadBalancer.class);
    }

    public static ILoadBalancer get(String loadBalanceStrategy) {
        return SpiLoader.getInstance(ILoadBalancer.class, loadBalanceStrategy);
    }


}
