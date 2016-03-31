package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.consumer.netty.decode.EventRequestDecoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class HttpRequestHandler extends MessageToMessageDecoder<HttpObject> {

    private EventRequestDecoder decoder;

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            Object eventRequest = decoder.decode(fullHttpRequest);
            out.add(eventRequest);
        }
    }

    public void setDecoder(EventRequestDecoder decoder) {
        this.decoder = decoder;
    }
}
