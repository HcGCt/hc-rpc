package com.hc.rpc.common;

import java.io.Serializable;

/**
 * @Author hc
 */
public class RpcResponse implements Serializable {

    private Object result;
    private Class<?> resultClazz;
    private String msg;
    private Exception exception;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Class<?> getResultClazz() {
        return resultClazz;
    }

    public void setResultClazz(Class<?> resultClazz) {
        this.resultClazz = resultClazz;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
