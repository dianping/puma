package com.dianping.puma.pumaserver.router.decoder;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DeprecatedDeprecatedBinlogGetRequestDecoderTest {

    @Test
    public void test_match() throws Exception {
        DeprecatedBinlogQueryDecoder target = new DeprecatedBinlogQueryDecoder();
        Assert.assertTrue(target.match(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/puma/channel/acceptor?op=")));
        Assert.assertFalse(target.match(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/puma/channel2/acceptor?op=")));
    }

    @Test
    public void test_convert_query_string() throws Exception {
        DeprecatedBinlogQueryDecoder target = new DeprecatedBinlogQueryDecoder();

        Map<String, String> result = target.getQueryStringMap("seq=-100&dt=a,b,&dt=c");

        Assert.assertEquals("-100", result.get("seq"));
        Assert.assertEquals("a,b,c", result.get("dt"));
    }
}