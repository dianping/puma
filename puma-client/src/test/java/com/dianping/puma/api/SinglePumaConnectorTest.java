package com.dianping.puma.api;

import com.dianping.puma.api.exception.PumaClientException;
import com.dianping.puma.core.dto.BinlogMessage;
import com.google.gson.Gson;
import org.junit.Test;

/**
 * Dozer @ 7/2/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SinglePumaConnectorTest {

    @Test
    public void testConnect() {
        SinglePumaClient connector = new SinglePumaClient("my-client", "127.0.0.1", 4040);
        connector.subscribe(true, false, false, "test", "a", "b");

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
        SinglePumaClient connector = new SinglePumaClient("dozer", "127.0.0.1", 4040);
        connector.subscribe(true, true, true, "test", "debug");

        while (true) {
            try {
                BinlogMessage message = connector.get(1);
                //todo:业务逻辑
                if ((System.currentTimeMillis() / 1000) % 1000 == 0) {
                    System.out.println(new Gson().toJson(message));
                }
                connector.ack(message.getLastBinlogInfo());
            } catch (PumaClientException exp) {
                exp.printStackTrace();
            }
        }
    }

}