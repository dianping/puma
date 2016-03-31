package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.model.message.EventUnsubscribeRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class EventUnsubscribeHandler extends SimpleChannelInboundHandler<EventUnsubscribeRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventUnsubscribeRequest msg) throws Exception {

    }
}
