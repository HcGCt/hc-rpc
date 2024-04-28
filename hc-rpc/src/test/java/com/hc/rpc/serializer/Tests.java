package com.hc.rpc.serializer;

import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.protocol.serialize.JsonSerializer;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Author hc
 */
public class Tests {

    @Test
    public void testJsonSerializer() throws IOException {
        JsonSerializer jsonSerializer = new JsonSerializer();
        RpcRequest request = new RpcRequest();
        request.setVersion("2.0");
        request.setParameters(new Object[]{"张三"});
        request.setParameterTypes(new Class[]{String.class});
        byte[] serialize = jsonSerializer.serialize(request);
        System.out.println(Arrays.toString(serialize));

        RpcRequest deserialize = jsonSerializer.deserialize(serialize, RpcRequest.class);
        System.out.println();
    }
}
