package com.hc.rpc.fault.tolerant;

import com.hc.rpc.common.RpcResponse;

import java.util.Map;

/**
 * @Author hc
 */
public interface ITolerantStrategy {

    RpcResponse handler(Map<String, Object> context, Throwable e);
}
