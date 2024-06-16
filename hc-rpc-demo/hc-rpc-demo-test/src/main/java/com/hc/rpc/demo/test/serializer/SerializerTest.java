package com.hc.rpc.demo.test.serializer;

import com.hc.rpc.protocol.serialize.HessianSerializer;
import com.hc.rpc.protocol.serialize.ISerializer;
import com.hc.rpc.protocol.serialize.JsonSerializer;

import java.io.IOException;

/**
 * @Author hc
 */
public class SerializerTest {

    public static void main(String[] args) throws IOException {
        MessageInfo messageInfo = MessageInfo.buildMessage();
        testSerializer(messageInfo, new ProtostuffSerializer(), "Protostuff");
        testSerializer(messageInfo, new KryoSerializer(), "Kryo");
        testSerializer(messageInfo, new HessianSerializer(), "Hessian");
        testSerializer(messageInfo, new JsonSerializer(), "Json");
        testSerializer(messageInfo, new JDKSerializer(), "JDK");
    }

    private static void testSerializer(MessageInfo messageInfo, ISerializer serializer, String name) throws IOException {
        byte[] bytes = serializer.serialize(messageInfo);
        System.out.println(name + " 序列化后的长度: " + bytes.length);
        // 序列化与反序列化10000次时间
        long cur = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            bytes = serializer.serialize(messageInfo);
            MessageInfo deserialize = serializer.deserialize(bytes, MessageInfo.class);
        }
        long now = System.currentTimeMillis();
        System.out.println(name + " 10000次时间: " + (now - cur) + "ms");
        System.out.println("###################################");
        System.out.println();
    }
}
