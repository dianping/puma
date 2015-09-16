package com.dianping.puma.utils;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class SocketHelper {
    private SocketHelper() {
    }

    public static String getIp(Channel channel) {
        InetSocketAddress address = getInetSocketAddress(channel);
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }

    public static InetSocketAddress getInetSocketAddress(Channel channel) {
        if (channel == null
                || channel.remoteAddress() == null
                || !(channel.remoteAddress() instanceof InetSocketAddress)) {
            return null;
        }
        return (InetSocketAddress) channel.remoteAddress();
    }
}
