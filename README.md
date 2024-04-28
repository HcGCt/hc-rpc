# RPC

## 系统设计

![](https://cccblogimgs.oss-cn-hangzhou.aliyuncs.com/rpc%E6%9E%B6%E6%9E%84.png)

## 各模块详解

- **loadbalance负载均衡策略**

  - ConsistentHashLoadBalancer：一致性哈希算法
  - RoundRobinLoadBalancer：轮询策略

- **protocol协议层**，定义消息传输的格式等

  - codec
    - Decoder:解码器，根据消息个数解码，通过固定`ByteBuf`标记，解决半包粘包的问题，即要求一次性解码一个完整的消息，否则指针回退
    - Encoder:编码器，消息体分魔数、版本、消息状态等；消息体直接根据序列化器下序列化为字节数组
  - serialize：序列化为字节码，或者反序列化为对象
  - RpcMessage：网络传输消息，包括消息头与消息体
  - MsgHeader：消息体头

- **invoker服务调用方**

  - RpcInvoker：Netty客户端监听及发送客户端请求
  - RpcInvokerProxy：客户端服务调用代理，服务调用基于代理模式，客户端使用动态代理对象调用请求的方法，此处编写调用方发起请求的流程：封装请求、负载均衡、重试机制、容错机制
  - RpcReferenceBean：服务调用方入口类，动态代理对象以便发起远程调用
  - RpcResponseHandler：Netty的ChannelHandler，执行接收响应的处理器逻辑，在编解码之后执行

- **provider服务提供方**

  - RpcProviderFactory：服务提供工厂，负责启动Netty服务器，添加服务调用接口实现
  - ThreadPoolFactory：服务端线程池，执行请求处理逻辑
  - RpcRequestHandler：Netty的ChannelHandler，执行接收请求的的处理器逻辑，在编解码之后执行

- **registry注册中心**

  - RedistryFactory：注册中心工厂，获取具体的注册中心
  - RedisRegistryCenter：Redis作为注册中心
  - LoaclRegistryCenter：本地注册中心
  - 其他TODO

- **fault容错机制**

  SPI解耦TODO

- **SPI机制**

  允许在运行时动态地加载实现特定接口的类，而不需要在代码中显式地指定该类，从而实现解耦和灵活性。实现高效的组件化和模块化，提高组件的扩展性。

  - SpiLoader：基于类加载、反射机制
    - 加载实现类
    - 获取具体接口实例