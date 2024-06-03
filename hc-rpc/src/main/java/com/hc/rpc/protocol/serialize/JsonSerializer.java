package com.hc.rpc.protocol.serialize;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.common.RpcResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @Author hc
 */
public class JsonSerializer implements ISerializer {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return obj instanceof String ? ((String) obj).getBytes() : MAPPER.writeValueAsBytes(obj);
    }

    /**
     * jackson在反序列化对象时为LinkedHashMap，特殊处理
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        T obj = MAPPER.readValue(data, clz);
        if (obj instanceof RpcRequest) {
            return convertRequest((RpcRequest) obj, clz);
        } else if (obj instanceof RpcResponse) {
            return convertResponse((RpcResponse) obj, clz);
        }
        return obj;
    }

    private <T> T convertRequest(RpcRequest rpcRequest, Class<T> clz) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            // 如果类型不同，则重新处理一下类型
            if (!clazz.isAssignableFrom(parameters[i].getClass())) {
                byte[] argBytes = MAPPER.writeValueAsBytes(parameters[i]);
                parameters[i] = MAPPER.readValue(argBytes, clazz);
            }
        }
        return clz.cast(rpcRequest);
    }

    private <T> T convertResponse(RpcResponse rpcResponse, Class<T> clz) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(rpcResponse.getResult());
        rpcResponse.setResult(MAPPER.readValue(bytes, rpcResponse.getResultClazz()));
        return clz.cast(rpcResponse);
    }
}
