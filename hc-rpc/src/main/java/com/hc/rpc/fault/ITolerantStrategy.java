package com.hc.rpc.fault;

import java.util.Map;

/**
 * TODO 容错机制解耦
 * @Author hc
 */
public interface ITolerantStrategy {

    void handler(Map<String, Object> context, Throwable e);
}
