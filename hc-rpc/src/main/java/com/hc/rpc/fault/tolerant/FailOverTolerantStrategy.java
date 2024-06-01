package com.hc.rpc.fault.tolerant;

import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.common.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * 故障转移
 *
 * @Author hc
 */
public class FailOverTolerantStrategy implements ITolerantStrategy {
    private Logger logger = LoggerFactory.getLogger(FailOverTolerantStrategy.class);

    @Override
    public RpcResponse handler(Map<String, Object> context, Throwable e) {
        Integer count = (Integer) context.get("count");
        Collection<ProviderMeta> otherProviderMetas = (Collection<ProviderMeta>) context.get("otherProviderMetas");
        ProviderMeta targetProviderMeta = (ProviderMeta) context.get("targetProviderMeta");
        logger.warn("rpc 调用失败,,触发 FailOver 策略， 第{}次重试,异常信息:{}", count, e.getMessage());

        count++;
        context.put("count", count);
        if (otherProviderMetas != null && otherProviderMetas.size() > 0) {
            final ProviderMeta next = otherProviderMetas.iterator().next();
            targetProviderMeta = next;
            otherProviderMetas.remove(next);
        } else {
            logger.warn("rpc 调用失败,无服务可用 providerName: {}, 异常信息: {}", targetProviderMeta.getName(), e.getMessage());
            throw new RuntimeException("服务调用失败");
        }
        return null;
    }
}
