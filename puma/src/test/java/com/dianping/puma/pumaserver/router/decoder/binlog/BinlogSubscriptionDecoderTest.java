package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * Dozer @ 7/6/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogSubscriptionDecoderTest {
    BinlogSubscriptionDecoder target = new BinlogSubscriptionDecoder();

    @Test
    public void test_match_and_decode() throws Exception {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/puma/binlog/subscribe?clientName=a&database=b&table=x&table=y&table=z&ddl=true&dml=true&transaction=true");

        Assert.assertTrue(target.match(request));

        BinlogSubscriptionRequest result = (BinlogSubscriptionRequest) target.decode(request);

        Assert.assertNotNull(result);
        Assert.assertEquals("a", result.getClientName());
        Assert.assertEquals("b", result.getDatabase());
        Assert.assertArrayEquals(new String[]{"x", "y", "z"}, result.getTables().toArray());
        Assert.assertEquals(true, result.isDdl());
        Assert.assertEquals(true, result.isDml());
        Assert.assertEquals(true, result.isTransaction());
    }
}