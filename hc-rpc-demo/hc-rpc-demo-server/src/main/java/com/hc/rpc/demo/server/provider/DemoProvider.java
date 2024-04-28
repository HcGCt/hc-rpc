package com.hc.rpc.demo.server.provider;

import com.hc.rpc.demo.client.service.DemoService;
import com.hc.rpc.demo.server.impl.DemoServiceImpl;
import com.hc.rpc.provider.RpcProviderFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Author hc
 */
public class DemoProvider {

    public static void main(String[] args) {
        RpcProviderFactory providerFactory = new RpcProviderFactory();
        providerFactory.setServerPort(12333);
        providerFactory.addService(DemoService.class.getSimpleName(), null, new DemoServiceImpl());

        providerFactory.start();

        // while (!Thread.currentThread().isInterrupted()) {
        //     TimeUnit.HOURS.sleep(1);
        // }

        // stop
        // providerFactory.stop();
    }
}
