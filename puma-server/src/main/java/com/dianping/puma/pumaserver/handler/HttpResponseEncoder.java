package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.util.ConvertHelper;
import com.google.common.net.MediaType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCounted;

import java.util.List;

/**
 * Dozer @ 7/1/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class HttpResponseEncoder extends MessageToMessageEncoder<Object> {
    public static final HttpResponseEncoder INSTANCE = new HttpResponseEncoder();

    private EventCodec codec = EventCodecFactory.createCodec("raw");

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            if (response instanceof ReferenceCounted) {
                ((ReferenceCounted) response).retain();
                out.add(response);
            }
        } else {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

            if (msg instanceof BinlogGetResponse) {
                BinlogGetResponse resp = (BinlogGetResponse) msg;
                BinlogGetRequest req = resp.getBinlogGetRequest();

                if ("raw".equals(req.getCodec())) {
                    response.headers().add(HttpHeaders.Names.CONTENT_TYPE, MediaType.OCTET_STREAM);
                    byte[] data = codec.encodeList(resp.getBinlogMessage().getBinlogEvents());
                    response.content().writeBytes(data);
                    response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, data.length);
                    out.add(response);
                    return;
                }
            }

            response.headers().add(HttpHeaders.Names.CONTENT_TYPE, MediaType.JSON_UTF_8);
            byte[] data = ConvertHelper.toBytes(msg);
            response.content().writeBytes(data);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, data.length);
            out.add(response);
        }
    }
}
