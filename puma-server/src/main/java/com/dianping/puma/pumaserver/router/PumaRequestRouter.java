package com.dianping.puma.pumaserver.router;

import com.dianping.puma.pumaserver.router.decoder.*;
import com.dianping.puma.pumaserver.router.decoder.binlog.BinlogAckDecoder;
import com.dianping.puma.pumaserver.router.decoder.binlog.BinlogGetDecoder;
import com.dianping.puma.pumaserver.router.decoder.binlog.BinlogSubscriptionDecoder;
import com.dianping.puma.pumaserver.router.decoder.binlog.BinlogUnsubscriptionDecoder;
import com.dianping.puma.pumaserver.router.decoder.deprecated.DeprecatedBinlogQueryDecoder;
import com.dianping.puma.pumaserver.router.decoder.status.StatusQueryDecoder;
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
        decoders.add(new BinlogSubscriptionDecoder());
        decoders.add(new BinlogUnsubscriptionDecoder());
        decoders.add(new BinlogGetDecoder());
        decoders.add(new BinlogAckDecoder());
        decoders.add(new DeprecatedBinlogQueryDecoder());
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