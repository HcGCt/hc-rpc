package com.hc.rpc.invoker;

import com.hc.rpc.common.Beat;
import com.hc.rpc.common.RpcFuture;
import com.hc.rpc.common.RpcResponse;
import com.hc.rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import static com.hc.rpc.common.RpcRequestHolder.REQUEST_MAP;

/**
 * 请求响应处理器
 *
 * @Author hc
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcMessage<RpcResponse>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<RpcResponse> msg) throws Exception {
        long requestId = msg.getHeader().getRequestId();
        RpcResponse response = msg.getBody();
        handleResponse(requestId, response);
    }


    public void handleResponse(long requestId, RpcResponse response) {
        RpcFuture<RpcResponse> responseFuture = REQUEST_MAP.get(requestId);
        if (responseFuture == null) {
            return;
        }

        if (responseFuture.getInvokeCallback() != null) {
            RpcInvokeCallback invokeCallback = responseFuture.getInvokeCallback();
            // callback类型
            RpcInvokerFactory.getInstance().executeCallback(new Runnable() {
                @Override
                public void run() {
                    if (response.getException() != null) {
                        invokeCallback.onFailure(response.getException());
                    } else {
                        invokeCallback.onSuccess(response.getResult());
                    }
                }
            });
        } else {
            // 其他
            responseFuture.getPromise().setSuccess(response);
        }

        REQUEST_MAP.remove(requestId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 检测到空闲，向服务端发送心跳包
            ctx.writeAndFlush(Beat.BEAT_PING).sync();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
