package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.util.ConvertHelper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCounted;

import java.util.List;

/**
 * Dozer @ 7/1/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class HttpResponseEncoder extends MessageToMessageEncoder<Object> {
    public static final HttpResponseEncoder INSTANCE = new HttpResponseEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            if (response instanceof ReferenceCounted) {
                ((ReferenceCounted) response).retain();
                out.add(response);
            }
        } else {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            byte[] data = ConvertHelper.toBytes(msg);
            response.content().writeBytes(data);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, data.length);
            out.add(response);
        }
    }
}
