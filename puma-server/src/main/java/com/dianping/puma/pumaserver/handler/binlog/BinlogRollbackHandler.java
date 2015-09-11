package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogAckRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogRollbackHandler extends SimpleChannelInboundHandler<BinlogAckRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinlogAckRequest msg) throws Exception {
        //todo;
    }
}
