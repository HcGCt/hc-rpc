package com.hc.rpc.fault;

import com.hc.rpc.spi.SpiLoader;

/**
 * @Author hc
 */
public class TolerantStrategyFactory {
    static {
        SpiLoader.load(ITolerantStrategy.class);
    }

    public static ITolerantStrategy get(String tolerantStrategy) {
        return SpiLoader.getInstance(ITolerantStrategy.class, tolerantStrategy);
    }
}
