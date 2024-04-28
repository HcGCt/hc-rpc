package com.hc.rpc.config;


import com.hc.rpc.common.constants.Constants;
import com.hc.rpc.utils.ConfigUtils;
import com.hc.rpc.utils.IpUtil;

import static com.hc.rpc.common.constants.Constants.*;

/**
 * @Author hc
 */
public class RpcConfig {
    private String serverHost = IpUtil.getLocalAddress().getHostAddress();
    private Integer serverPort = 8080;
    private String version = "1.0";
    private String serialization = HESSIAN_SERIALIZATION;   // 默认序列化机制
    private String loadBalanceStrategy = ROUND_ROBIN;       // 默认负载均衡策略
    private String faultTolerantStrategy = FailFast;        // 默认容错机制
    /**
     * 注册中心配置
     */
    // 注册中心地址
    private String registerAddress = "127.0.0.1:6379";
    // 注册中心类型
    private String registerType = REDIS_REGISTRY;           // 默认注册中心
    // 注册中心用户名及密码
    private String registerUsername = "";
    private String registerPsw = "";
    private static volatile RpcConfig instance;

    static {
        try {
            instance = ConfigUtils.loadConfig(RpcConfig.class, DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 加载失败使用默认配置
            instance = new RpcConfig();
        }
    }

    public static RpcConfig getInstance() {
        return instance;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getRegisterUsername() {
        return registerUsername;
    }

    public void setRegisterUsername(String registerUsername) {
        this.registerUsername = registerUsername;
    }

    public String getRegisterPsw() {
        return registerPsw;
    }

    public void setRegisterPsw(String registerPsw) {
        this.registerPsw = registerPsw;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public String getFaultTolerantStrategy() {
        return faultTolerantStrategy;
    }

    public void setFaultTolerantStrategy(String faultTolerantStrategy) {
        this.faultTolerantStrategy = faultTolerantStrategy;
    }

    public static void setInstance(RpcConfig instance) {
        RpcConfig.instance = instance;
    }
}
