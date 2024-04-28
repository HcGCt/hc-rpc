package com.hc.rpc.protocol.serialize;

import java.io.IOException;

/**
 * @Author hc
 */
public interface ISerializer {
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
