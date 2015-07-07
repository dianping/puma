package com.dianping.puma.pumaserver.remote;

import io.netty.channel.Channel;

/**
 * Dozer @ 5/20/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ChannelHolder {
    boolean add(Channel channel);

    boolean remove(Object o);
}
