package com.hc.rpc.protocol.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author hc
 */
public class KryoSerializer implements ISerializer {
    // Kryo非线程安全类
    private static final ThreadLocal<Kryo> kryoLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);    // 不需提前预注册类
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        Output output = new Output(bos, 1024);
        kryoLocal.get().writeObject(output, obj);
        output.flush();
        return bos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        Input input = new Input(bis);
        return kryoLocal.get().readObject(input, clz);
    }
}
