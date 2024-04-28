package com.hc.rpc.spi;

import com.hc.rpc.loadbalance.ILoadBalancer;
import com.hc.rpc.protocol.serialize.ISerializer;
import com.hc.rpc.registry.IRegistryCenter;
import com.hc.rpc.registry.RegistryFactory;
import org.junit.Test;

/**
 * @Author hc
 */
public class SPITests {

    @Test
    public void test() {
        SpiLoader.load(IRegistryCenter.class);
        IRegistryCenter registryCenter = SpiLoader.getInstance(IRegistryCenter.class, "redis");

        SpiLoader.load(ILoadBalancer.class);
        ILoadBalancer loadBalancer = SpiLoader.getInstance(ILoadBalancer.class, "consistentHash");

        SpiLoader.load(ISerializer.class);
        ISerializer serializer = SpiLoader.getInstance(ISerializer.class, "json");
        System.out.println();
    }
}
