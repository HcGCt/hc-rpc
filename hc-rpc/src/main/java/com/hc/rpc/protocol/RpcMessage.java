package com.hc.rpc.protocol;

import java.io.Serializable;

/**
 * 消息
 * @Author hc
 */
public class RpcMessage<T> implements Serializable {
    private MsgHeader header;       // 消息头
    private T body;                 // 消息体

    public RpcMessage() {

    }
    public RpcMessage(MsgHeader header, T body) {
        this.header = header;
        this.body = body;
    }

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
