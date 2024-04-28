package com.hc.rpc.invoker;

/**
 * 异步回调基类
 *
 * @Author hc
 */
public abstract class RpcInvokeCallback<T> {
    public abstract void onSuccess(T result);

    public abstract void onFailure(Throwable exception);


    // 存储RpcInvokeCallback
    private static ThreadLocal<RpcInvokeCallback> threadLocalInvokerCallback = new ThreadLocal<>();

    public static RpcInvokeCallback getCallback() {
        RpcInvokeCallback invokeCallback = threadLocalInvokerCallback.get();
        threadLocalInvokerCallback.remove();
        return invokeCallback;
    }
    public static void setCallback(RpcInvokeCallback invokeCallback) {
        threadLocalInvokerCallback.set(invokeCallback);
    }
    public static void removeCallback() {
        threadLocalInvokerCallback.remove();
    }
}
