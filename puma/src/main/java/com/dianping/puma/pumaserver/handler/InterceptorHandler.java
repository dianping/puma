package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.common.intercept.PumaInterceptor;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
@ChannelHandler.Sharable
public class InterceptorHandler extends ChannelDuplexHandler {

    private PumaInterceptor<BinlogHttpMessage> pumaInterceptor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinlogHttpMessage) {
            BinlogHttpMessage binlogHttpMessage = (BinlogHttpMessage) msg;
            pumaInterceptor.before(binlogHttpMessage);
        }

        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof BinlogHttpMessage) {
            BinlogHttpMessage binlogHttpMessage = (BinlogHttpMessage) msg;
            pumaInterceptor.after(binlogHttpMessage);
        }

        ctx.write(msg, promise);
    }

    public void setPumaInterceptor(PumaInterceptor<BinlogHttpMessage> pumaInterceptor) {
        this.pumaInterceptor = pumaInterceptor;
    }
}
