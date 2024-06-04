package com.hc.rpc.common;

import com.hc.rpc.common.constants.MsgType;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.protocol.MsgHeader;
import com.hc.rpc.protocol.RpcMessage;

import static com.hc.rpc.common.constants.Constants.MAGIC;
import static com.hc.rpc.common.constants.Constants.VERSION;

/**
 * @Author hc
 */
public class Beat {
    public static final int BEAT_INTERVAL = 30;
    public static final long BEAT_ID = -1;

    public static RpcMessage<RpcRequest> BEAT_PING;

    static {
        BEAT_PING = new RpcMessage<>();
        MsgHeader header = new MsgHeader();
        header.setRequestId(BEAT_ID);
        header.setMagic(MAGIC);
        header.setVersion(VERSION);
        byte[] serialization = RpcConfig.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        BEAT_PING.setHeader(header);
        BEAT_PING.setBody(new RpcRequest());
    }
}
