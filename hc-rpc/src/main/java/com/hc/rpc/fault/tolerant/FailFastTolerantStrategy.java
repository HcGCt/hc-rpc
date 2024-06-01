package com.hc.rpc.fault.tolerant;

import com.hc.rpc.common.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 快速失败
 *
 * @Author hc
 */
public class FailFastTolerantStrategy implements ITolerantStrategy {
    private Logger logger = LoggerFactory.getLogger(FailFastTolerantStrategy.class);

    @Override
    public RpcResponse handler(Map<String, Object> context, Throwable e) {
        logger.warn("rpc 调用失败,触发 FailFast 策略,异常信息: {}", e.getMessage());
        throw new RuntimeException(e.getMessage());
    }
}
