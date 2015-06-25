package com.dianping.puma.pumaserver.router;

import com.dianping.puma.pumaserver.router.decoder.BinlogQueryDecoder;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
import com.dianping.puma.pumaserver.router.decoder.StatusQueryDecoder;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaRequestRouter {
    private Set<RequestDecoder> decoders = new LinkedHashSet<RequestDecoder>();

    public PumaRequestRouter() {
        decoders.add(new StatusQueryDecoder());
        decoders.add(new BinlogQueryDecoder());
    }

    public Object route(FullHttpRequest request) {
        for (RequestDecoder decoder : decoders) {
            if (decoder.match(request)) {
                return decoder.decode(request);
            }
        }
        return null;
    }
}