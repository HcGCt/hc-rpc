package com.hc.rpc.provider;

import com.hc.rpc.common.Beat;
import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Author hc
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcMessage<RpcRequest>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<RpcRequest> requestRpcMessage) throws Exception {
        // 心跳
        if (Beat.BEAT_ID == requestRpcMessage.getHeader().getRequestId()) {
            return;
        }

        // 本ChannelHandler将请求交给线程池处理
        ServerThreadPoolFactory.submitRequest(ctx, requestRpcMessage);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();      // 检测到IdleStateEvent,释放资源
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
