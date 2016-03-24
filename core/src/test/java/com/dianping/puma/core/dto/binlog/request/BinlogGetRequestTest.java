package com.dianping.puma.core.dto.binlog.request;

import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 7/17/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogGetRequestTest {
    @Test
    public void testTimeout() throws Exception {
        BinlogGetRequest request;

        request = new BinlogGetRequest().setTimeout(0).setTimeUnit(TimeUnit.SECONDS).setStartTime(System.currentTimeMillis());
        Assert.assertFalse(request.isTimeout());

        request = new BinlogGetRequest().setTimeout(10).setTimeUnit(null).setStartTime(System.currentTimeMillis());
        Assert.assertFalse(request.isTimeout());

        request = new BinlogGetRequest().setTimeout(1).setTimeUnit(TimeUnit.SECONDS).setStartTime(System.currentTimeMillis());
        Assert.assertFalse(request.isTimeout());

        request = new BinlogGetRequest().setTimeout(1).setTimeUnit(TimeUnit.SECONDS).setStartTime(System.currentTimeMillis() - 2000);
        Assert.assertTrue(request.isTimeout());

        request = new BinlogGetRequest().setTimeout(1).setTimeUnit(TimeUnit.MILLISECONDS).setStartTime(System.currentTimeMillis() - 2);
        Assert.assertTrue(request.isTimeout());
    }
}