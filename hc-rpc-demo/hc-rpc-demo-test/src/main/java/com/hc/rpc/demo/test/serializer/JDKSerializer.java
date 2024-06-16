package com.hc.rpc.demo.test.serializer;

import com.hc.rpc.protocol.serialize.ISerializer;

import java.io.*;

/**
 * @Author hc
 */
public class JDKSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        objectOutputStream.close();
        byteArrayOutputStream.close();
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        T object = null;
        try {
            object = (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        objectInputStream.close();
        byteArrayInputStream.close();
        return object;
    }
}
