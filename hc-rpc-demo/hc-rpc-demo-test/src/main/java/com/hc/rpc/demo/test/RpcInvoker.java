package com.hc.rpc.demo.test;

import com.hc.rpc.demo.client.service.DemoService;
import com.hc.rpc.invoker.CallType;
import com.hc.rpc.invoker.RpcInvokeCallback;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;

/**
 * @Author hc
 */
public class RpcInvoker {
    private RpcReferenceBean rpcReferenceBean;
    private int request;
    private String type;
    private String param;
    private DemoService service;

    public RpcInvoker(String type, String address, int request, String param) {
        rpcReferenceBean = new RpcReferenceBean();
        rpcReferenceBean.setTimeout(3000);
        if ("SYN".equals(type)) {
            rpcReferenceBean.setCallType(CallType.SYNC);
        } else if ("CALLBACK".equals(type)) {
            rpcReferenceBean.setCallType(CallType.CALLBACK);
        }
        rpcReferenceBean.setServerAddress(address);
        this.request = request;
        this.type = type;
        this.param = param;
        try {
            this.service = rpcReferenceBean.getObject(DemoService.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object request() throws Exception {
        Object res = null;
        if (request == 1) {
            res = service.sayHello(param);
        } else if (request == 2) {
            res = service.getStudent(param);
        }
        return res;
    }
}
