package com.hc.rpc.utils;

/**
 * @Author hc
 */
public class RpcStringUtil {

    public static String buildProviderKey(String name, String version) {
        return String.join("$", name, version);
    }
}
