package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SimplePumaClientTest {

    @Test(expected = PumaClientException.class)
    public void testNotThreadSafe() throws Exception {
        final AtomicReference<SimplePumaClient> reference = new AtomicReference<SimplePumaClient>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                reference.set(new SimplePumaClient(new PumaClientConfig()));
                System.out.println("init puma client");
            }
        }).start();

        while (reference.get() == null) {
            Thread.sleep(10);
        }

        reference.get().get(1);
    }
}