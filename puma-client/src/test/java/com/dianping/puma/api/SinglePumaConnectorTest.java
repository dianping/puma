package com.dianping.puma.api;

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
    public void testConnect() throws Exception {
        SinglePumaConnector connector = new SinglePumaConnector("test", "www.dozer.cc", 80);
        connector.connect();
        connector.subscribe(true, true, true, "test", "user");
        System.in.read();
    }
}