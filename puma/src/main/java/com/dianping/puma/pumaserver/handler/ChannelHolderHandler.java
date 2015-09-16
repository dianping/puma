package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.pumaserver.remote.ChannelHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Dozer @ 5/20/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@ChannelHandler.Sharable
public class ChannelHolderHandler extends ChannelInboundHandlerAdapter {
    private final ChannelHolder channelHolder;

    public ChannelHolderHandler(ChannelHolder channelHolder) {
        this.channelHolder = channelHolder;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelHolder.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelHolder.remove(ctx.channel());
        super.channelInactive(ctx);
    }
}
