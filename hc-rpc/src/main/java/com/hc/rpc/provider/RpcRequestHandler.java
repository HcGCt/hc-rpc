package com.hc.rpc.provider;

import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author hc
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcMessage<RpcRequest>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<RpcRequest> requestRpcMessage) throws Exception {
        // 本ChannelHandler将请求交给线程池处理
        ServerThreadPoolFactory.submitRequest(ctx, requestRpcMessage);
    }
}
