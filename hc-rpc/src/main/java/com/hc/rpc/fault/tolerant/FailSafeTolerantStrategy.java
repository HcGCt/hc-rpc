package com.hc.rpc.fault.tolerant;

import com.hc.rpc.common.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 忽略故障
 *
 * @Author hc
 */
public class FailSafeTolerantStrategy implements ITolerantStrategy {
    private Logger logger = LoggerFactory.getLogger(FailSafeTolerantStrategy.class);

    @Override
    public RpcResponse handler(Map<String, Object> context, Throwable e) {
        logger.warn("rpc 调用失败,触发 FailSafe 策略,异常信息: {}", e.getMessage());
        context.put("count", Integer.MAX_VALUE);
        return new RpcResponse();
    }
}
