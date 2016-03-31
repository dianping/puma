package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.model.message.EventRollbackRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class EventRollbackHandler extends SimpleChannelInboundHandler<EventRollbackRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventRollbackRequest msg) throws Exception {

    }
}
