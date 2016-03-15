package com.dianping.puma.server.netty.decode;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public interface EventRequestDecoder {

    Object decode(FullHttpRequest request);
}
