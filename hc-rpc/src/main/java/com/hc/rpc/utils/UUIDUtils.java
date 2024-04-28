package com.hc.rpc.utils;

import java.util.UUID;

/**
 * @Author hc
 */
public class UUIDUtils {
    public static String createUUID16() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static String createUUID32() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    public static void main(String[] args) {
        String uuid16 = UUIDUtils.createUUID16();
        System.out.println(uuid16);
    }
}
