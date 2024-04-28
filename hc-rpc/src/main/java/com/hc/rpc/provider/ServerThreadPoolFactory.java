package com.hc.rpc.provider;

import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.common.RpcResponse;
import com.hc.rpc.common.constants.MsgStatus;
import com.hc.rpc.common.constants.MsgType;
import com.hc.rpc.protocol.MsgHeader;
import com.hc.rpc.protocol.RpcMessage;
import com.hc.rpc.utils.RpcStringUtil;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务端线程池工厂
 *
 * @Author hc
 */
public class ServerThreadPoolFactory {
    private static Logger logger = LoggerFactory.getLogger(ServerThreadPoolFactory.class);

    private static ThreadPoolExecutor slowThreadPool;

    private static ThreadPoolExecutor fastThreadPool;

    private static volatile ConcurrentHashMap<String, AtomicInteger> slowTaskMap = new ConcurrentHashMap<>();

    private static int coreSize = Runtime.getRuntime().availableProcessors();

    // 服务缓存...todo 待优化,统一管理
    private static Map<String, Object> rpcServiceMap;

    public static void stop() {
        if (slowThreadPool != null) {
            slowThreadPool.shutdown();
        }
        if (fastThreadPool != null) {
            fastThreadPool.shutdown();
        }
        logger.info("服务端线程池关闭....");
    }
    public static void init() {
        // 线程池均采用默认拒绝策略
        slowThreadPool = new ThreadPoolExecutor(
                coreSize / 2, coreSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(2000),
                r -> {
                    Thread thread = new Thread(r, "rpc-server-rpc-slow-thread-pool-" + r.hashCode());
                    thread.setName("rpc-server-rpc-slow-thread-pool-" + r.hashCode());
                    // thread.setDaemon(true);
                    return thread;
                }
        );
        fastThreadPool = new ThreadPoolExecutor(
                coreSize, 2 * coreSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                // 线程工厂
                r -> {
                    Thread thread = new Thread(r, "rpc-server-fast-thread-pool-" + r.hashCode());
                    // thread.setDaemon(true); // 守护线程
                    return thread;
                }
        );
        startClearMonitor();
    }

    /**
     * 清理慢请求
     */
    private static void startClearMonitor() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            slowTaskMap.clear();
        }, 5, 5, TimeUnit.MINUTES);
    }

    private ServerThreadPoolFactory() {
    }

    public static void setRpcServiceMap(Map<String, Object> rpcMap) {
        rpcServiceMap = rpcMap;
    }

    // 提交request
    public static void submitRequest(ChannelHandlerContext ctx, RpcMessage<RpcRequest> requestRpcMessage) {
        RpcRequest request = requestRpcMessage.getBody();
        String key = request.getClazzName() + request.getMethodName() + request.getVersion();
        ThreadPoolExecutor pool = fastThreadPool;
        // 慢任务
        if (slowTaskMap.containsKey(key) && slowTaskMap.get(key).intValue() >= 10) {
            pool = slowThreadPool;
        }
        pool.execute(() -> {
            RpcMessage<RpcResponse> responseRpcMessage = new RpcMessage<>();
            final MsgHeader header = requestRpcMessage.getHeader();
            RpcResponse response = new RpcResponse();
            long startTime = System.currentTimeMillis();

            // 处理request
            try {
                final Object result = submit(ctx, requestRpcMessage);
                response.setResult(result);
                response.setResultClazz(result == null ? null : result.getClass());
                header.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            } catch (Exception e) {
                // 执行业务失败则将异常返回
                header.setStatus((byte) MsgStatus.FAILED.ordinal());
                response.setException(e);
                logger.error("process request {} error", header.getRequestId(), e);
            } finally {
                long cost = System.currentTimeMillis() - startTime;
                if (cost > 1000) {
                    final AtomicInteger timeOutCount = slowTaskMap.putIfAbsent(key, new AtomicInteger(1));
                    if (timeOutCount != null) {
                        timeOutCount.incrementAndGet();
                    }
                }
            }
            responseRpcMessage.setHeader(header);
            responseRpcMessage.setBody(response);
            logger.info("执行成功: {},{},{},{}",
                    Thread.currentThread().getName(), request.getClazzName(), request.getMethodName(), request.getVersion());
            // 传给下一个ChannelHandler
            // ctx.fireChannelRead(responseRpcMessage);
            ctx.writeAndFlush(responseRpcMessage);
        });
    }

    private static Object submit(ChannelHandlerContext ctx, RpcMessage<RpcRequest> rpcMessage) throws Exception {
        MsgHeader header = rpcMessage.getHeader();
        header.setMsgType((byte) MsgType.RESPONSE.ordinal());
        final RpcRequest request = rpcMessage.getBody();
        // 执行具体业务
        return handle(request);
    }

    // 调用方法,代理对象
    private static Object handle(RpcRequest request) throws Exception {
        String serviceKey = RpcStringUtil.buildProviderKey(request.getClazzName(), request.getVersion());
        // 获取服务信息
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClazzName(), request.getMethodName()));
        }

        // 反射
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // FastClass fastClass = FastClass.create(serviceClass);
        // int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        // fastClass.invoke(methodIndex, serviceBean, parameters);

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result = method.invoke(serviceBean, parameters);
        // 调用方法并返回结果
        return result;
    }

}
