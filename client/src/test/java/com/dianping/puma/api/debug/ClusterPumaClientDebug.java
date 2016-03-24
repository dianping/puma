package com.dianping.puma.api.debug;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.Event;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClientDebug {

    public static void main(String[] args) throws InterruptedException, IOException {

        PumaClient client = new PumaClientConfig()
                .setClientName("dozer-test")
                .setDatabase("UnifiedOrder0")
                .setTables(Lists.newArrayList("UOD_Order0"))
                .buildClusterPumaClient();

        while (true) {
            try {
                BinlogMessage message = client.get(1, 1, TimeUnit.SECONDS);

                for (Event event : message.getBinlogEvents()) {
                    System.out.println(event);
                }

                client.ack(message.getLastBinlogInfo());
            } catch (Throwable t) {

            }
        }
    }
}
