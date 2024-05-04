package com.hc.rpc.invoker;


import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.registry.RegistryFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @Author hc
 */
public class RpcInvokerFactory {

    private static volatile RpcInvokerFactory instance;

    private RpcInvokerFactory() {
    }

    public static RpcInvokerFactory getInstance() {
        if (instance == null) {
            synchronized (RpcInvokerFactory.class) {
                instance = new RpcInvokerFactory();
            }
        }
        return instance;
    }


    // 异步回调线程池
    private static volatile ThreadPoolExecutor responseCallbackThreadPool = null;

    public void executeCallback(Runnable runnable) {
        // 双检锁
        if (responseCallbackThreadPool == null) {
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    int coreSize = Runtime.getRuntime().availableProcessors();
                    responseCallbackThreadPool = new ThreadPoolExecutor(
                            coreSize / 2,
                            coreSize,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingDeque<>(1000),
                            r -> {
                                Thread thread = new Thread(r, "rpc-callback-handler-thread-pool-" + r.hashCode());
                                // thread.setDaemon(true); // 守护线程
                                return thread;
                            }
                    );
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }

    public static void stop() {
        RegistryFactory.get(RpcConfig.getInstance().getRegisterType()).destroy();
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
        RpcInvoker.getInstance().shutdown();
    }



    // ------------ createRpcReferenceBean ------------
    public static RpcReferenceBean createRpcReferenceBean(long timeout) {
        RpcReferenceBean rpcReferenceBean = new RpcReferenceBean();
        rpcReferenceBean.setTimeout(timeout);
        rpcReferenceBean.setCallType(CallType.SYNC);
        return rpcReferenceBean;
    }

    public static RpcReferenceBean createRpcReferenceBean(long timeout, CallType callType, RpcInvokeCallback invokeCallback) {
        RpcReferenceBean rpcReferenceBean = new RpcReferenceBean();
        rpcReferenceBean.setTimeout(timeout);
        rpcReferenceBean.setCallType(callType);
        rpcReferenceBean.setInvokeCallback(invokeCallback);
        return rpcReferenceBean;
    }

    public static RpcReferenceBean createRpcReferenceBean(
            String version,
            long timeout,
            String loadBalanceStrategy,
            String faultTolerantStrategy,
            CallType callType,
            RpcInvokeCallback invokeCallback) {
        RpcReferenceBean rpcReferenceBean = new RpcReferenceBean();
        rpcReferenceBean.setVersion(version);
        rpcReferenceBean.setTimeout(timeout);
        rpcReferenceBean.setLoadBalanceStrategy(loadBalanceStrategy);
        rpcReferenceBean.setFaultTolerantStrategy(faultTolerantStrategy);
        rpcReferenceBean.setCallType(callType);
        rpcReferenceBean.setInvokeCallback(invokeCallback);
        return rpcReferenceBean;
    }

}
