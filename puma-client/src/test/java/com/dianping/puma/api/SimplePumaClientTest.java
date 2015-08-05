package com.dianping.puma.api;

import com.dianping.puma.api.impl.SimplePumaClient;
import com.dianping.puma.core.dto.BinlogMessage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Dozer @ 7/2/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SimplePumaClientTest {

    @Test
    public void testConnect() {
        SimplePumaClient connector = new SimplePumaClient("my-client", "127.0.0.1:4040");
        List<String> tables = new ArrayList<String>();
        tables.add("a");
        tables.add("b");
        connector.subscribe("test", tables, true, false, false);

        while (true) {
            try {
                BinlogMessage message = connector.getWithAck(1);


                Thread.sleep(1000);

                //todo: 业务逻辑
                System.out.println(message);
            } catch (PumaClientException exp) {
                //todo: add log
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void testConnectSync() {
        SimplePumaClient connector = new SimplePumaClient("dozer", "127.0.0.1:4040");
        List<String> tables = new ArrayList<String>();
        tables.add("debug");
        connector.subscribe("test", tables, true, true, true);

        final int size = 1;

        while (true) {
            try {
                BinlogMessage message = connector.get(size);
                System.out.println(message.getLastBinlogInfo().toString());
                connector.ack(message.getLastBinlogInfo());
            } catch (PumaClientException exp) {
                exp.printStackTrace();
            }
        }
    }

}