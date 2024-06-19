package com.hc.rpc.protocol.serialize;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;

/**
 * protostuff基于protobuf实现的序列化方法，不用写.proto文件
 *
 * @Author hc
 */
public class ProtostuffSerializer implements ISerializer {
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        byte[] bytes = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
        BUFFER.clear();
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        Schema<T> schema = RuntimeSchema.getSchema(clz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}
