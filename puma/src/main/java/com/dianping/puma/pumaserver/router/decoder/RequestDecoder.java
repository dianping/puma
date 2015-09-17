package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.pumaserver.exception.DecoderException;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface RequestDecoder {

    boolean match(FullHttpRequest request);

    Object decode(FullHttpRequest request) throws DecoderException;
}
