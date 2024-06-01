package com.hc.rpc.utils;

/**
 * @Author hc
 */
public class RpcStringUtil {

    public static String buildProviderKey(String s1, String s2) {
        return String.join(":", s1, s2);
    }
}
