package com.dianping.puma.server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class TestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            System.out.println(fullHttpRequest);
            ReferenceCountUtil.release(msg);
        }
    }
}
