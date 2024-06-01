package com.hc.rpc.invoker;

import com.hc.rpc.config.RpcConfig;

import java.lang.reflect.Proxy;

/**
 * 服务调用方入口
 *
 * @Author hc
 */
public class RpcReferenceBean {

    private String version;
    private long timeout;
    private String loadBalanceStrategy;     // 负载均衡策略
    private String faultTolerantStrategy;   // 容错机制
    private int retryCount = 3;                 // 重试次数
    private CallType callType = CallType.SYNC;  // 默认同步调用
    private RpcInvokeCallback invokeCallback;

    private String serverAddress;       // 若确定服务端ip,则无负载均衡

    public RpcReferenceBean() {
        RpcConfig rpcConfig = RpcConfig.getInstance();
        this.version = rpcConfig.getVersion();
        this.faultTolerantStrategy = rpcConfig.getFaultTolerantStrategy();
        this.loadBalanceStrategy = rpcConfig.getLoadBalanceStrategy();
    }

    // 反射获取服务动态代理对象
    public <T> T getObject(Class<T> clazz) throws Exception {
        if (clazz == null) {
            throw new UnsupportedOperationException("未执行服务接口");
        }
        // 根据代理RpcInvokerProxy获取服务代理
        T service = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new RpcInvokerProxy(version, timeout, loadBalanceStrategy, faultTolerantStrategy, retryCount, callType, invokeCallback, serverAddress)
        );
        return service;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public void setInvokeCallback(RpcInvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public void setFaultTolerantStrategy(String faultTolerantStrategy) {
        this.faultTolerantStrategy = faultTolerantStrategy;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
