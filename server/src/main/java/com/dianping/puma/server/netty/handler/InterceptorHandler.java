package com.dianping.puma.server.netty.handler;

import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.server.intercept.PumaEventServerInterceptor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class InterceptorHandler extends ChannelDuplexHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaEventServerInterceptor interceptor;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof BinlogHttpMessage) {
            try {
                BinlogHttpMessage binlogHttpMessage = (BinlogHttpMessage) msg;
                interceptor.after(binlogHttpMessage);
            } catch (Throwable t) {
                logger.error("Failed to do after in interceptor handler for msg[%s].", msg);
            }
        }

        ctx.write(msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinlogHttpMessage) {
            try {
                BinlogHttpMessage binlogHttpMessage = (BinlogHttpMessage) msg;
                interceptor.before(binlogHttpMessage);
            } catch (Throwable t) {
                logger.error("Failed to do before in interceptor handler for msg[%s].", msg);
            }
        }

        ctx.fireChannelRead(msg);
    }

    public void setInterceptor(PumaEventServerInterceptor interceptor) {
        this.interceptor = interceptor;
    }
}
