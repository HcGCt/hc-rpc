# RPC

## 系统设计

![](https://cccblogimgs.oss-cn-hangzhou.aliyuncs.com/rpc.png)

这是一个基于Netty实现的简易RPC框架，具备一个RPC框架的基本功能，封装了底层通信细节，调用远程服务就像调用本地服务。可扩展实现注册中心、序列化方式、负载均衡等以覆盖默认实现的策略。

## 目录

```
|—— hc-rpc: rpc核心代码
	|—— common: 公共实体类，包括常量、枚举、RPC请求类、RPC响应类等
	|—— config: rpc公共配置类
	|—— fault.tolerant: 容错机制，默认实现快速失败、故障转移、忽略三种策略
	|—— invoker: 服务调用方，以代理方式发起网络请求
	|—— loadbalance: 负载均衡策略，默认实现轮询、一致性哈希
	|—— protocol: 协议层，定义消息格式、序列化方式以及编解码逻辑
	|—— provider: 服务提供方，Netty多Reactor模型，请求处理逻辑采用反射调用
	|—— registry: 注册中心，默认实现zookeeper、Etcd注册中心逻辑
	|—— spi: SPI机制，动态加载注册中心、序列化方式、负载均衡、故障转移等，用户可自行扩展
	|—— utils: 工具类
|—— hc-rpc-demo: rpc简单用例
	|—— hc-rpc-demo-client: 客户端，即invoker
	|—— hc-rpc-demo-server: 服务端，即provider
```

##  使用

1. 首先导入依赖，需install本地仓库

   ```xml
   <dependency>
       <groupId>com.hc</groupId>
       <artifactId>hc-rpc</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   ```

2. 定义远程调用接口

   ```java
   public interface DemoService {
       String sayHello(String name);
   }
   ```

3. 服务端

   需实现服务接口

   ```java
   public class DemoServiceImpl implements DemoService {
       @Override
       public String sayHello(String name) {
           System.out.println(name + " hello!");
           return name;
       }
   }
   ```

   启动服务端：

   ```java
   public static void main(String[] args) {
       RpcProviderFactory providerFactory = new RpcProviderFactory();
       providerFactory.setServerPort(9090);
       providerFactory.addService(DemoService.class.getSimpleName(), null, new DemoServiceImpl());
       providerFactory.start();
   }
   ```

4. 客户端

   ```java
   public static void testSYN() throws Exception {
       RpcReferenceBean rpcReferenceBean = new RpcReferenceBean();
       rpcReferenceBean.setTimeout(1000);
       rpcReferenceBean.setCallType(CallType.SYNC);
       DemoService service = rpcReferenceBean.getObject(DemoService.class);
       String hello = service.sayHello("张三");
       System.out.println(hello);
   }
   ```

## todo list

- [x] Netty通信方式
  - [x] 自定义协议TCP通信
  - [ ] HTTP
- [x] 调用方案
  - [x] SYNC
  - [x] CALLBACK 
  - [ ] ONEWAY
  - [ ] FUTURE
- [x] 注册中心
  - [x] Zookeeper
  - [x] Etcd
  - [ ] Nacos
  - [ ] ...
- [x] 序列化
  - [x] Json
  - [x] Hessian
  - [ ] Protostuff
  - [ ] Kryo
- [x] 负载均衡
  - [x] 轮询策略
  - [x] 一致性哈希
  - [ ] 随机策略
- [x] 容错机制
  - [x] 重试
  - [x] 快速失败
  - [x] 故障转移
  - [x] 忽略错误
  - [ ] 降级
- [x] 配置
  - [x] properties文件配置
  - [ ] JVM参数配置
  - [ ] Apollo动态配置
- [x] 多版本
- [x] SPI机制
- [ ] 过滤器
- [ ] 服务分组
- [ ] 服务监控
- [ ] ......



