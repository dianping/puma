package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.model.message.EventAckRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by xiaotian.li on 16/3/12.
 * Email: lixiaotian07@gmail.com
 */
public class EventAckHandler extends SimpleChannelInboundHandler<EventAckRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventAckRequest msg) throws Exception {

    }
}
