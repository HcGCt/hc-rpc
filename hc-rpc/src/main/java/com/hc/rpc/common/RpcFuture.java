package com.hc.rpc.common;

import com.hc.rpc.invoker.RpcInvokeCallback;
import io.netty.util.concurrent.Promise;

/**
 * future
 *
 * @Author hc
 */
public class RpcFuture<T> {

    private Promise<T> promise;
    private long timeout;
    private RpcInvokeCallback invokeCallback;


    public RpcFuture() {
    }

    public RpcFuture(Promise<T> promise, long timeout) {
        this.promise = promise;
        this.timeout = timeout;
    }


    public RpcFuture(Promise<T> promise, long timeout, RpcInvokeCallback invokeCallback) {
        this.promise = promise;
        this.timeout = timeout;
        this.invokeCallback = invokeCallback;
    }

    public Promise<T> getPromise() {
        return promise;
    }

    public void setPromise(Promise<T> promise) {
        this.promise = promise;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public RpcInvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public void setInvokeCallback(RpcInvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
    }
}
