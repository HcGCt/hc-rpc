package com.hc.rpc.common.constants;

/**
 * @Author hc
 */
public enum MsgType {

    REQUEST,
    RESPONSE,
    HEARTBEAT;

    public static MsgType findByType(int type) {
        return MsgType.values()[type];
    }
}
