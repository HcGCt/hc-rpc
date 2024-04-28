package com.hc.rpc.invoker;

import com.hc.rpc.common.ProviderMate;
import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.protocol.RpcMessage;

/**
 * @Author hc
 */
public interface IRpcInvoker {

    void sendRequest(RpcMessage<RpcRequest> rpcMessage, ProviderMate providerMate) throws Exception;

}
