package com.hc.rpc.invoker;

import com.hc.rpc.common.ProviderMate;
import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.protocol.RpcMessage;
import com.hc.rpc.protocol.codec.Decoder;
import com.hc.rpc.protocol.codec.Encoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc 调用者
 *
 * @Author hc
 */
public class RpcInvoker implements IRpcInvoker {
    private Logger logger = LoggerFactory.getLogger(RpcInvoker.class);
    private final Bootstrap bootstrap;                      // 客户端
    private final EventLoopGroup eventLoopGroup;            // 事件循环组,处理channel上的IO事件
    private Channel channel;
    private RpcInvoker() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new Encoder())
                                .addLast(new Decoder())
                                .addLast(new RpcResponseHandler());
                    }
                });

    }

    private static volatile RpcInvoker invoker;
    public static RpcInvoker getInstance() {
        if (invoker == null) {
            synchronized (RpcInvoker.class) {
                invoker = new RpcInvoker();
            }
        }
        return invoker;
    }

    /**
     * 发送请求
     *
     * @param rpcMessage
     * @param providerMate
     * @throws Exception
     */
    @Override
    public void sendRequest(RpcMessage<RpcRequest> rpcMessage, ProviderMate providerMate) throws Exception {
        if (providerMate != null) {
            // 连接
            String ip = providerMate.getAddress().split(":")[0];
            int port = Integer.parseInt(providerMate.getAddress().split(":")[1]);
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (future.isSuccess()) {
                        logger.info("连接 rpc server 成功, addr: {}", providerMate.getAddress());
                    } else {
                        logger.error("连接 rpc server 失败, addr: {}", providerMate.getAddress());
                        future.cause().printStackTrace();
                        eventLoopGroup.shutdownGracefully();
                    }
                }
            });
            future.channel().writeAndFlush(rpcMessage);
        }
    }

    public void shutdown() {
        try {
            if (this.channel != null && this.channel.isActive()) {
                this.channel.close().sync();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully();
            }
        }
    }

    public void close() {
        if (this.channel != null && this.channel.isActive()) {
            this.channel.close();
        }
        System.out.println(">>>>>>>>>>> rpc netty client close.");
    }
}
