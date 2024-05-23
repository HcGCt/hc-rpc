package com.hc.rpc.fault;

import java.util.Map;

/**
 * @Author hc
 */
public class FailFastTolerantStrategy implements ITolerantStrategy{
    @Override
    public void handler(Map<String, Object> context, Throwable e) {

    }
}
