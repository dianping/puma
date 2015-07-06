package com.dianping.puma.api;

import com.dianping.puma.api.connector.exception.PumaClientAuthException;
import com.dianping.puma.api.connector.exception.PumaClientException;
import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Dozer @ 7/2/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SinglePumaConnectorTest {

    @Test
    @Ignore
    public void testConnect() {
        SinglePumaConnector connector = new SinglePumaConnector("my-client", "127.0.0.1", 4040);

        while (true) {
            try {
                connector.connect();
                while (true) {
                    try {
                        try {
                            System.out.println(new Gson().toJson(connector.getWithAck(1)));
                        } catch (PumaClientException exp) {
                            connector.rollback();
                        }
                    } catch (PumaClientAuthException exp) {
                        try {
                            connector.subscribe(true, false, false, "test", "a", "b");
                        } catch (PumaClientException e) {
                            //todo: log
                        }
                    }
                }
            } catch (PumaClientException exp) {
                //todo:log
            }
        }
    }

}