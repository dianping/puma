package com.dianping.puma.server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpObject;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class HttpRequestHandler extends MessageToMessageDecoder<HttpObject> {

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {

    }
}
