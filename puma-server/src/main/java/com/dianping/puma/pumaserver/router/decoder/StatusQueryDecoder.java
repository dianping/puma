package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.StatusQuery;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.regex.Pattern;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class StatusQueryDecoder implements RequestDecoder {
    Pattern pattern = Pattern.compile("^/puma/channel/status.*$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) {
        return new StatusQuery();
    }
}
