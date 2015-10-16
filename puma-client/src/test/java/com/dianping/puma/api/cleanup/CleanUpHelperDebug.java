package com.dianping.puma.api.cleanup;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.core.dto.BinlogMessage;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CleanUpHelperDebug {
    @Test
    public void testCleanUp() throws Exception {
        DebugClass item = new DebugClass();
        item = null;
        System.gc();
        Thread.sleep(2000);
    }

    @Test
    public void testCluster() throws Exception {
        PumaClient client = new PumaClientConfig()
                .setClientName("dozer-debug")
                .setDatabase("UnifiedOrder0")
                .setTables(Lists.newArrayList("UOD_Order0"))
                .setServerHosts(Lists.newArrayList("192.168.216.78:4040"))
                .buildFixedClusterPumaClient();

        BinlogMessage message = client.get(1, 1, TimeUnit.SECONDS);

        client = null;

        System.gc();

        System.in.read();
    }

    static class DebugClass {
        public DebugClass() {
            CleanUpHelper.register(this, new CleanUpExt());
        }
    }

    static class CleanUpExt implements CleanUp {
        @Override
        public void cleanUp() {
            System.out.println("cleanUp");
        }
    }
}