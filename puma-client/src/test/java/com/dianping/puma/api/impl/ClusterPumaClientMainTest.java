package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClientMainTest {

    public static void main(String[] args) {
        List<String> tables = new ArrayList<String>();
        tables.add("UOD_Order0");
        tables.add("UOD_Order1");
        tables.add("UOD_OrderAdditionalFee0");
        tables.add("UOD_OrderAdditionalFee1");

        PumaClient client = new PumaClientConfig()
                .setClientName("dozer-debug")
                .setDatabase("UnifiedOrder0")
                .setTables(tables)
                .buildClusterPumaClient();

        final int size = 100;

        while (true) {
            try {
                BinlogMessage message = client.get(size, 5, TimeUnit.SECONDS);

                for (Event event : message.getBinlogEvents()) {
                    System.out.println(event.toString());
                }

                client.ack(message.getLastBinlogInfo());
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }
}
