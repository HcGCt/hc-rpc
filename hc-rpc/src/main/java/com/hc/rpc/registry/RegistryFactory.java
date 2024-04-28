package com.hc.rpc.registry;

import com.hc.rpc.spi.SpiLoader;

/**
 * @Author hc
 */
public class RegistryFactory {
    static {
        SpiLoader.load(IRegistryCenter.class);
    }

    public static IRegistryCenter get(String registryService) {
        return SpiLoader.getInstance(IRegistryCenter.class, registryService);
    }
}
