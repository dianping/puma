package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.netty.entity.StatusQuery;
import com.dianping.puma.core.util.ConvertHelper;
import com.dianping.puma.pumaserver.AttributeKeys;
import com.dianping.puma.pumaserver.client.ClientInfo;
import com.dianping.puma.pumaserver.client.ClientType;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class StatusQueryHandler extends SimpleChannelInboundHandler<StatusQuery> {
    public static StatusQueryHandler INSTANCE = new StatusQueryHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StatusQuery msg) throws Exception {
        ctx.channel().attr(AttributeKeys.CLIENT_INFO).set(new ClientInfo().setClientType(ClientType.PUMACLIENT));

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("Connection", "close");
        response.headers().add("Content-type", "application/json");

        Map<String, Object> status = new HashMap<String, Object>();
        status.put("serverStatus", SystemStatusContainer.instance.listServerStatus());
        status.put("serverDdlCounters", SystemStatusContainer.instance.listServerDdlCounters());
        status.put("serverRowDeleteCounters", SystemStatusContainer.instance.listServerRowDeleteCounters());
        status.put("serverRowInsertCounters", SystemStatusContainer.instance.listServerRowInsertCounters());
        status.put("serverRowUpdateCounters", SystemStatusContainer.instance.listServerRowUpdateCounters());
        status.put("clientStatus", SystemStatusContainer.instance.listClientStatus());
        status.put("storageStatus", SystemStatusContainer.instance.listStorageStatus());
        byte[] data = ConvertHelper.toBytes(status);

        response.headers().add("Content-Length", data.length);
        response.content().writeBytes(data);

        ctx.channel().writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
            }
        });
    }
}
