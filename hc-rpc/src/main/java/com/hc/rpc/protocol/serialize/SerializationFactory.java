package com.hc.rpc.protocol.serialize;

import com.hc.rpc.spi.SpiLoader;

/**
 * 序列化工厂
 *
 * @Author hc
 */
public class SerializationFactory {
    static {
        SpiLoader.load(ISerializer.class);
    }

    public static ISerializer get(String serialization) {
        return SpiLoader.getInstance(ISerializer.class, serialization);
    }
}
