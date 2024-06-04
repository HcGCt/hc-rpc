package com.hc.rpc.provider;

import com.hc.rpc.common.Beat;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.protocol.codec.Decoder;
import com.hc.rpc.protocol.codec.Encoder;
import com.hc.rpc.registry.IRegistry;
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
            EventLoopGroup boss = new NioEventLoopGroup(1);
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
                                        // 服务端N*2时间内没有读写时间，则触发IdleStateEvent事件
                                        .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL * 2, TimeUnit.SECONDS))
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

    public void stop() {
        // 通过打断停止线程而非stop
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
        IRegistry registry = RegistryFactory.get(registerType);
        registry.destroy();
    }


    // start前添加service
    public void addService(String serviceName, String version, Object serviceBean) {
        init();
        if (version == null || version.length() == 0) {
            version = "1.0";
        }
        try {
            ProviderMeta providerMeta = new ProviderMeta();
            providerMeta.setAddress(IpUtil.getIpPort(serverHost, serverPort));
            providerMeta.setVersion(version);
            providerMeta.setName(serviceName);
            // 注册服务
            IRegistry registry = RegistryFactory.get(registerType);
            registry.register(providerMeta);

            // 缓存
            String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
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
