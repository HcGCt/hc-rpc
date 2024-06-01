package com.hc.rpc.demo.client.invoker;

import com.hc.rpc.demo.client.dto.Student;
import com.hc.rpc.demo.client.service.DemoService;
import com.hc.rpc.invoker.CallType;
import com.hc.rpc.invoker.RpcInvokeCallback;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;


/**
 * @Author hc
 */
public class DemoInvoker {
    public static void main(String[] args) throws InterruptedException {
        try {
            testSYN();
            // testCallback();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            Thread.sleep(3000);
            RpcInvokerFactory.stop();
        }

    }


    public static void testSYN() throws Exception {
        RpcReferenceBean rpcReferenceBean = new RpcReferenceBean();
        rpcReferenceBean.setTimeout(10000000);
        rpcReferenceBean.setCallType(CallType.SYNC);
        // rpcReferenceBean.setServerAddress("192.168.88.1:9999");
        DemoService service = rpcReferenceBean.getObject(DemoService.class);

        Student student = service.getStudent("张三");
        System.out.println(student);
    }

    public static void testCallback() throws Exception {
        RpcReferenceBean rpcReferenceBean = new RpcReferenceBean();
        rpcReferenceBean.setTimeout(10000000);
        // rpcReferenceBean.setServerAddress("192.168.88.1:9999");
        rpcReferenceBean.setCallType(CallType.CALLBACK);
        DemoService service = rpcReferenceBean.getObject(DemoService.class);

        RpcInvokeCallback.setCallback(new RpcInvokeCallback() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("回调回调回调回调结果：" + result);
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println(exception.getMessage());
            }
        });

        String hello = service.sayHello("李四");
    }
}
