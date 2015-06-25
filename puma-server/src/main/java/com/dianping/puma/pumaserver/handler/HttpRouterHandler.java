package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.pumaserver.router.PumaRequestRouter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;

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
            if (request != null) {
                out.add(result);
            }
        }
    }
}
