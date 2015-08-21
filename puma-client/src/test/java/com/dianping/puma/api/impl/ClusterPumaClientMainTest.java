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
        tables.add("UOD_Order2");
        tables.add("UOD_Order3");
        tables.add("UOD_OrderExtraFields2");
        tables.add("UOD_OrderExtraFields3");
        tables.add("UOD_OrderLog2");
        tables.add("UOD_OrderLog3");
        tables.add("UOD_OrderPaymentDetail2");
        tables.add("UOD_OrderPaymentDetail3");
        tables.add("UOD_OrderSKU2");
        tables.add("UOD_OrderSKU3");
        tables.add("UOD_OrderSKUExtraFields2");
        tables.add("UOD_OrderSKUExtraFields3");

        PumaClient client = new PumaClientConfig()
                .setClientName("dozer-debug")
                .setDatabase("UnifiedOrder1")
                .setTables(tables)
                .setServerHost("192.168.216.79:4040")
                .buildSimplePumaClient();

        final int size = 100;

        while (true) {
            try {
                BinlogMessage message = client.get(size, 100, TimeUnit.SECONDS);

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
