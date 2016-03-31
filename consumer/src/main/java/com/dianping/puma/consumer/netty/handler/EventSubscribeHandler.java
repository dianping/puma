package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.model.message.EventSubscribeRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class EventSubscribeHandler extends SimpleChannelInboundHandler<EventSubscribeRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventSubscribeRequest msg) throws Exception {
        String clientName = msg.getClientName();
    }
}
