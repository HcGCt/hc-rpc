package com.hc.rpc.invoker;

import com.hc.rpc.common.*;
import com.hc.rpc.common.constants.MsgType;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.protocol.MsgHeader;
import com.hc.rpc.protocol.RpcMessage;
import com.hc.rpc.loadbalance.ILoadBalancer;
import com.hc.rpc.loadbalance.LoadBalancerFactory;
import com.hc.rpc.loadbalance.ProviderMateRes;
import com.hc.rpc.utils.IpUtil;
import com.hc.rpc.utils.RpcStringUtil;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.hc.rpc.common.RpcRequestHolder.REQUEST_ID_GEN;
import static com.hc.rpc.common.constants.Constants.*;

/**
 * 服务调用代理
 *
 * @Author hc
 */
public class RpcInvokerProxy implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(RpcInvokerProxy.class);

    private String version;
    private long timeout;                   // 超时时间
    private String loadBalanceStrategy;     // 负载均衡策略
    private String faultTolerantStrategy;   // 容错机制
    private int retryCount;                 // 重试次数

    private CallType callType;              // 异步调用
    private RpcInvokeCallback invokeCallback;
    private String serverAddress;           // 若确定服务端ip,则无负载均衡

    public RpcInvokerProxy(String version, long timeout, String loadBalanceStrategy,
                           String faultTolerantStrategy, int retryCount,
                           CallType callType, RpcInvokeCallback invokeCallback, String serverAddress) {
        this.version = version;
        this.timeout = timeout;
        this.loadBalanceStrategy = loadBalanceStrategy;
        this.faultTolerantStrategy = faultTolerantStrategy;
        this.retryCount = retryCount;
        this.callType = callType;
        this.invokeCallback = invokeCallback;
        this.serverAddress = serverAddress;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcMessage<RpcRequest> rpcMessage = new RpcMessage<>();
        // 消息头
        MsgHeader header = new MsgHeader();
        long requestId = REQUEST_ID_GEN.incrementAndGet();
        header.setRequestId(requestId);
        header.setMagic(MAGIC);
        header.setVersion(VERSION);
        byte[] serialization = RpcConfig.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        rpcMessage.setHeader(header);

        // 消息体
        RpcRequest request = new RpcRequest();
        request.setVersion(version);
        request.setClazzName(method.getDeclaringClass().getSimpleName());
        request.setMethodName(method.getName());
        request.setParameters((args == null || args.length == 0) ? new Object[0] : args);
        request.setParameterTypes(method.getParameterTypes());
        // request.setServiceAttachments(RpcProperties.getInstance().getServerAttachments());
        // request.setClientAttachments(RpcProperties.getInstance().getClientAttachments());
        rpcMessage.setBody(request);

        // 拦截器 todo


        RpcInvoker invoker = RpcInvoker.getInstance();
        String providerName = RpcStringUtil.buildProviderKey(request.getClazzName(), request.getVersion());
        ProviderMate targetProviderMate = null;
        Collection<ProviderMate> otherProviderMates = null;
        if (serverAddress != null && serverAddress.length() > 0) {
            targetProviderMate = new ProviderMate();
            targetProviderMate.setName(providerName);
            targetProviderMate.setAddress(serverAddress);
        } else {
            // 获取负载均衡策略,必须有注册中心
            final ILoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalanceStrategy);
            ProviderMateRes providerMateRes = loadBalancer.select(args, providerName);
            targetProviderMate = providerMateRes.getCur();
            otherProviderMates = providerMateRes.getOthers();
        }
        long count = 1;     // 请求次数
        long retryCount = this.retryCount;
        RpcResponse rpcResponse = null;
        // 重试机制
        while (count <= retryCount) {
            // 发送请求
            try {
                // todo 异步同步
                if (CallType.SYNC == callType) {
                    RpcFuture<RpcResponse> rpcFuture = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout, null);
                    RpcRequestHolder.REQUEST_MAP.put(requestId, rpcFuture);
                    invoker.sendRequest(rpcMessage, targetProviderMate);
                    // 等待,同步
                    rpcResponse = rpcFuture.getPromise().get(rpcFuture.getTimeout(), TimeUnit.MILLISECONDS);
                    if (rpcResponse.getException() != null && (otherProviderMates == null || otherProviderMates.size() == 0)) {
                        throw rpcResponse.getException();
                    }
                    if (rpcResponse.getException() != null) {
                        throw rpcResponse.getException();
                    }
                    logger.debug("rpc 调用成功, providerName: {}", providerName);
                    return rpcResponse.getResult();
                } else if (CallType.CALLBACK == callType) {
                    // todo test
                    RpcInvokeCallback rpcInvokeCallback = invokeCallback;
                    RpcInvokeCallback threadLocalInvokeCallback = RpcInvokeCallback.getCallback();
                    if (threadLocalInvokeCallback != null) {
                        rpcInvokeCallback = threadLocalInvokeCallback;
                    }
                    if (rpcInvokeCallback == null) {
                        throw new RuntimeException("RpcInvokeCallback（CallType=" + CallType.CALLBACK.name() + "） 不能为 null.");
                    }
                    RpcFuture<RpcResponse> rpcFuture = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout, rpcInvokeCallback);
                    RpcRequestHolder.REQUEST_MAP.put(requestId, rpcFuture);
                    invoker.sendRequest(rpcMessage, targetProviderMate);
                    return null;
                }

                // todo 过滤


            } catch (Throwable e) {
                String errorMsg = e.toString();
                // 容错机制 todo

                switch (faultTolerantStrategy) {
                    // 快速失败
                    case FailFast:
                        logger.warn("rpc 调用失败,触发 FailFast 策略,异常信息: {}", errorMsg);
                        return rpcResponse.getException();
                    // 故障转移
                    case Failover:
                        logger.warn("rpc 调用失败,第{}次重试,异常信息:{}", count, errorMsg);
                        count++;
                        if (otherProviderMates != null && otherProviderMates.size() > 0) {
                            final ProviderMate next = otherProviderMates.iterator().next();
                            targetProviderMate = next;
                            otherProviderMates.remove(next);
                        } else {
                            logger.warn("rpc 调用失败,无服务可用 providerName: {}, 异常信息: {}", providerName, errorMsg);
                            throw new RuntimeException("服务调用失败");
                        }
                        break;
                    // 忽视这次错误
                    case Failsafe:
                        return null;
                }
            }
        }

        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }


    // todo 异步相关
}
