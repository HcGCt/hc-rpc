package com.hc.rpc.provider;

import com.hc.rpc.common.ProviderMate;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.protocol.codec.Decoder;
import com.hc.rpc.protocol.codec.Encoder;
import com.hc.rpc.registry.IRegistryCenter;
import com.hc.rpc.registry.RegistryFactory;
import com.hc.rpc.utils.IpUtil;
import com.hc.rpc.utils.RpcStringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 服务提供工厂
 *
 * @Author hc
 */
public class RpcProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(RpcProviderFactory.class);

    private static final Map<String, Object> rpcServiceMap = new HashMap<>();


    private Thread serverThread;
    private String serverHost;
    private Integer serverPort;
    private String registerAddress;
    private String registerType;

    public void start() {
        if (rpcServiceMap.isEmpty()) {
            throw new UnsupportedOperationException("未添加service");
        }
        ServerThreadPoolFactory.init();
        serverThread = new Thread(() -> {
            EventLoopGroup boss = new NioEventLoopGroup();
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            // socket
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new Encoder())
                                        .addLast(new Decoder())
                                        .addLast(new RpcRequestHandler());
                            }
                        })
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture channelFuture = bootstrap.bind(serverHost, serverPort).sync();
                logger.info("rpc server started!  host: {}, port: {}", serverHost, serverPort);
                // 等待关闭
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    logger.info("rpc server stop! ");
                } else {
                    logger.error("rpc server error.", e);
                }
            } finally {
                ServerThreadPoolFactory.stop();
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }
        });
        // serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stop(){
        // 通过打断停止线程而非stop
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
        IRegistryCenter registryCenter = RegistryFactory.get(registerType);
        registryCenter.destroy();
    }


    // start前添加service
    public void addService(String serviceName, String version, Object serviceBean) {
        init();
        if (version == null || version.length() == 0) {
            version = "1.0";
        }
        try {
            ProviderMate providerMate = new ProviderMate();
            providerMate.setAddress(IpUtil.getIpPort(serverHost, serverPort));
            providerMate.setVersion(version);
            providerMate.setName(serviceName);
            // 注册服务
            IRegistryCenter registryCenter = RegistryFactory.get(registerType);
            registryCenter.register(providerMate);

            // 缓存
            String key = RpcStringUtil.buildProviderKey(providerMate.getName(), providerMate.getVersion());
            rpcServiceMap.put(key, serviceBean);
            logger.info("register server {} version {}", serviceName, version);
        } catch (Exception e) {
            logger.error("failed to register service {}", version, e);
        }
        ServerThreadPoolFactory.setRpcServiceMap(rpcServiceMap);
    }

    // 参数优先级:初始化指定 -> 配置文件 -> 默认参数
    private void init() {
        if (serverHost == null || serverHost.trim().length() == 0) {
            serverHost = RpcConfig.getInstance().getServerHost();
        }
        if (serverPort == null) {
            serverPort = RpcConfig.getInstance().getServerPort();
        }
        if (registerAddress == null || registerAddress.trim().length() == 0) {
            registerAddress = RpcConfig.getInstance().getRegisterAddress();
        }
        if (registerType == null || registerType.trim().length() == 0) {
            registerType = RpcConfig.getInstance().getRegisterType();
        }
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }


    public String getServerHost() {
        return serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public String getRegisterType() {
        return registerType;
    }
}
