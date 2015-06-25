package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.BinlogQuery;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogQueryDecoderTest {

    @Test
    public void test_match() throws Exception {
        BinlogQueryDecoder target = new BinlogQueryDecoder();
        Assert.assertTrue(target.match(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/puma/channel/acceptor?op=")));
        Assert.assertFalse(target.match(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/puma/channel2/acceptor?op=")));
    }

    @Test
    public void test_decode() throws Exception {
        BinlogQueryDecoder target = new BinlogQueryDecoder();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/puma/channel/acceptor?seq=-100&dt=a,b,c");

        BinlogQuery result = (BinlogQuery) target.decode(request);

        Assert.assertEquals(-100, result.getSeq());
        Assert.assertEquals("a", result.getDatabaseTables()[0]);
        Assert.assertEquals("b", result.getDatabaseTables()[1]);
        Assert.assertEquals("c", result.getDatabaseTables()[2]);
    }
}