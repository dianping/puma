package com.dianping.puma.core.netty.handler;

import com.dianping.puma.core.util.ConvertHelper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.List;

/**
 * Dozer @ 7/1/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class HttpEntityEncoder extends MessageToMessageEncoder<Object> {
    public static final HttpEntityEncoder INSTANCE = new HttpEntityEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        byte[] data = ConvertHelper.toBytes(msg);
        response.content().writeBytes(data);
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, data.length);
        out.add(response);
    }
}
