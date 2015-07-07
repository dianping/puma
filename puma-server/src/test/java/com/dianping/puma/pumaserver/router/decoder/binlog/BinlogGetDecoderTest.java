package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 7/6/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogGetDecoderTest {
    BinlogGetDecoder target = new BinlogGetDecoder();

    @Test
    public void test_match_and_decode() throws Exception {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/puma/binlog/get?token=1&clientName=xx&batchSize=10&timeout=100&timeUnit=SECONDS&autoAck=true");

        Assert.assertTrue(target.match(request));

        BinlogGetRequest result = (BinlogGetRequest) target.decode(request);

        Assert.assertNotNull(result);
        Assert.assertEquals("1", result.getToken());
        Assert.assertEquals("xx", result.getClientName());
        Assert.assertEquals(100, result.getTimeout());
        Assert.assertEquals(10, result.getBatchSize());
        Assert.assertEquals(TimeUnit.SECONDS, result.getTimeUnit());
        Assert.assertEquals(true, result.isAutoAck());
    }
}