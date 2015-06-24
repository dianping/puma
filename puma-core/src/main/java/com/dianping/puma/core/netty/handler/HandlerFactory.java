package com.dianping.puma.core.netty.handler;

import io.netty.channel.ChannelHandler;

import java.util.Map;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface HandlerFactory {
    Map<String, ChannelHandler> getHandlers();
}
