package com.dianping.puma.server.netty.decode;

import com.dianping.puma.server.exception.PumaEventRequestUriException;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedEventRequestDecoder implements EventRequestDecoder {

    private List<EventRequestDecoder> decoders;

    @Override
    public Object decode(FullHttpRequest request) {
        for (EventRequestDecoder decoder: decoders) {
            try {
                return decoder.decode(request);
            } catch (PumaEventRequestUriException ignore) {
            }
        }

        throw new PumaEventRequestUriException("Unmatched event request uri[%s].", request.getUri());
    }

    public void setDecoders(List<EventRequestDecoder> decoders) {
        this.decoders = decoders;
    }
}
