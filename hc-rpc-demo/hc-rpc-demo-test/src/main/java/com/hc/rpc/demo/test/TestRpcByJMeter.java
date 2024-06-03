package com.hc.rpc.demo.test;

import com.hc.rpc.invoker.RpcInvokerFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * @Author hc
 */
public class TestRpcByJMeter extends AbstractJavaSamplerClient {
    private RpcInvoker rpcInvoker;

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            long begin = System.currentTimeMillis();
            Object response = rpcInvoker.request();
            long cost = (System.currentTimeMillis() - begin);
            System.out.println(response.toString() + " cost:[" + cost + "ms]");
            if (response == null) {
                result.setSuccessful(false);
                return result;
            }
            result.setSuccessful(true);
        } catch (Exception e) {
            result.setSuccessful(false);
            e.printStackTrace();
        } finally {
            result.sampleEnd();
        }
        return result;
    }


    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("address", "127.0.0.1:9092");
        arguments.addArgument("type", "SYN");
        arguments.addArgument("request", "1");
        arguments.addArgument("param", "张三");
        return arguments;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        String address = context.getParameter("address");
        String type = context.getParameter("type");
        int request = context.getIntParameter("request");
        String name = context.getParameter("param");
        rpcInvoker = new RpcInvoker(type, address, request, name);
        super.setupTest(context);
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (rpcInvoker != null) {
            RpcInvokerFactory.stop();
        }
        super.teardownTest(context);
    }
}
