package com.hc.rpc.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

/**
 * @Author hc
 */
public class ZkTests {

    @Test
    public void watchTest() throws InterruptedException {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("1.94.40.102:8181")
                .retryPolicy(new ExponentialBackoffRetry(10000, 3))
                .build();
        client.start();
        CuratorCache curatorCache = CuratorCache.build(client, "/hc-rpc/registry/DemoService:1.0");
        curatorCache.listenable().addListener(
                CuratorCacheListener
                        .builder()
                        .forChanges((childData, childData1) -> System.out.println("Changes! " + childData + ", " + childData1))
                        .forDeletes(childData -> System.out.println("Deletes! " + childData))
                        .build()
        );

        curatorCache.start();
        while (true) {
            Thread.sleep(1000);
        }
    }
}
