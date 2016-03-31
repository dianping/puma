package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.model.message.EventGetRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class EventGetHandler extends SimpleChannelInboundHandler<EventGetRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EventGetRequest msg) throws Exception {

    }
}
