package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogAckRequest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Dozer @ 7/6/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogAckDecoderTest {
    BinlogAckDecoder target = new BinlogAckDecoder();

    @Test
    public void test_match_and_decode() throws Exception {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/puma/binlog/ack?token=1&clientName=xx&binlogFile=f1&binlogPosition=1&serverId=2&timestamp=3");

        Assert.assertTrue(target.match(request));

        BinlogAckRequest result = (BinlogAckRequest) target.decode(request);

        Assert.assertNotNull(result);
        Assert.assertEquals("1", result.getToken());
        Assert.assertEquals("xx", result.getClientName());
        Assert.assertEquals("f1", result.getBinlogAck().getBinlogInfo().getBinlogFile());
        Assert.assertEquals(1, result.getBinlogAck().getBinlogInfo().getBinlogPosition());
        Assert.assertEquals(2, result.getBinlogAck().getBinlogInfo().getServerId());
        Assert.assertEquals(3, result.getBinlogAck().getBinlogInfo().getTimestamp());
    }
}