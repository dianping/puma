package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.pumaserver.router.PumaRequestRouter;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;

import java.util.List;

/**
 * Dozer @ 6/24/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class HttpRouterHandler extends MessageToMessageDecoder<HttpObject> {
    public static HttpRouterHandler INSTANCE = new HttpRouterHandler();

    private final PumaRequestRouter router;

    public HttpRouterHandler() {
        this.router = new PumaRequestRouter();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            Object result = router.route(request);
            if (result != null) {
                out.add(result);
            } else {
                ctx.channel().writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.channel().close();
                    }
                });
            }
        }
    }
}
