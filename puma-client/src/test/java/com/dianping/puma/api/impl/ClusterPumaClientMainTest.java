package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.Event;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClientMainTest {

    public static void main(String[] args) {
        List<String> tables = new ArrayList<String>();
        tables.add("UOD_Order0");
        tables.add("UOD_Order1");
        tables.add("UOD_OrderExtraFields0");
        tables.add("UOD_OrderExtraFields1");
        tables.add("UOD_OrderLog0");
        tables.add("UOD_OrderLog1");
        tables.add("UOD_OrderPaymentDetail0");
        tables.add("UOD_OrderPaymentDetail1");
        tables.add("UOD_OrderSKU0");
        tables.add("UOD_OrderSKU1");
        tables.add("UOD_OrderSKUExtraFields0");
        tables.add("UOD_OrderSKUExtraFields1");

        PumaClient client = new PumaClientConfig()
                .setClientName("dozer-debug")
                .setDatabase("UnifiedOrder0")
                .setTables(tables)
                .setServerHosts(Lists.newArrayList("127.0.0.1:4040"))
                .buildFixedClusterPumaClient();

        final int size = 100;

        while (true) {
            try {
                BinlogMessage message = client.get(size, 1, TimeUnit.SECONDS);

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
