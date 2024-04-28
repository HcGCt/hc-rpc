package com.hc.rpc.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI机制，实现服务组件动载加载、扩展
 * todo
 * @Author hc
 */
public class SpiLoader {

    private static Logger logger = LoggerFactory.getLogger(SpiLoader.class);

    // 系统SPI目录
    private static final String RPC_SYS_SPI_DIR = "META-INF/hc-rpc/";
    // 用户自定义SPI目录
    private static final String RPC_DIY_SPI_DIR = "META-INF/rpc/";
    // 扫描路径
    private static final String[] SCAN_DIRS = new String[]{RPC_SYS_SPI_DIR, RPC_DIY_SPI_DIR};

    // 已经加载的类 k:接口名  v:实现类
    private static final Map<String, Map<String, Class<?>>> loadedMap = new ConcurrentHashMap<>();

    // 实例化对象缓存
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 加载某个类
     * 各自工厂类静态代码块调用,类加载原则：使用时才加载
     * 所以使用各种工厂类时才加载工厂类然后执行静态代码块,从而加载各自实现类
     *
     * @param clazz
     */
    public static void load(Class<?> clazz) {
        logger.info("SPI加载, 类型为: {}", clazz);
        // 扫描路径，优先用户自定义SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String dir : SCAN_DIRS) {
            String filePath = dir + clazz.getName();
            try {
                Enumeration<URL> enumeration = SpiLoader.class.getClassLoader().getResources(filePath);
                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArr = line.split("=");
                        if (strArr.length == 2) {
                            String key = strArr[0];
                            String name = strArr[1];
                            final Class<?> finalClazz = Class.forName(name);
                            keyClassMap.put(key, finalClazz);
                        }
                    }
                }
            } catch (Exception e) {
                logger.info("SPI load error.", e);
            }
        }
        loadedMap.put(clazz.getName(), keyClassMap);
    }


    public static <T> T getInstance(Class<?> clazz, String key) {
        String clazzName = clazz.getName();
        Map<String, Class<?>> classMap = loadedMap.get(clazzName);
        if (classMap == null) {
            throw new RuntimeException("SPI未加载类:" + clazzName);
        }
        if (!classMap.containsKey(key)) {
            throw new RuntimeException("SPI不在:" + key + "的类");
        }

        Class<?> implClass = classMap.get(key);
        // 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(implClassName + "类实例化失败");
            }
        }
        return (T) instanceCache.get(implClassName);
    }

    public static List<Object> getInstances(Class<?> clazz) {
        String clazzName = clazz.getName();
        Map<String, Class<?>> classMap = loadedMap.get(clazzName);
        List<Object> list = new ArrayList<>();
        if (classMap == null) {
            throw new RuntimeException("SPI未加载类:" + clazzName);
        }
        classMap.forEach((k, v) -> {
            String implClassName = v.getName();
            if (!instanceCache.containsKey(implClassName)) {
                try {
                    instanceCache.put(implClassName, v.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(implClassName + "类实例化失败");
                }
            }
            Object instance = instanceCache.get(implClassName);
            list.add(instance);
        });
        return list;
    }
}
