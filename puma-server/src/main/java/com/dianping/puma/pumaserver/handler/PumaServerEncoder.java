package com.dianping.puma.pumaserver.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.List;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class PumaServerEncoder extends MessageToMessageEncoder<Object> {
    public static PumaServerEncoder INSTANCE = new PumaServerEncoder();

    private static PumaHttpResponseEncoder httpResponseEncoder = new PumaHttpResponseEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof HttpResponse) {
            httpResponseEncoder.encodeRequest(ctx, msg, out);
        } else if (msg instanceof ByteBuf) {
            ((ByteBuf) msg).retain();
            out.add(msg);
        }
    }

    static class PumaHttpResponseEncoder extends HttpResponseEncoder {
        public void encodeRequest(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }
}
