package com.hc.rpc.registry;

import com.hc.rpc.spi.SpiLoader;

/**
 * @Author hc
 */
public class RegistryFactory {
    static {
        SpiLoader.load(IRegistry.class);
    }

    public static IRegistry get(String registryService) {
        return SpiLoader.getInstance(IRegistry.class, registryService);
    }
}
