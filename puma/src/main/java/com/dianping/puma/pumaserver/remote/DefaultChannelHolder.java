package com.dianping.puma.pumaserver.remote;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Random;

/**
 * Dozer @ 5/20/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultChannelHolder extends DefaultChannelGroup implements ChannelHolder {
    private final static Random random = new Random();

    public DefaultChannelHolder() {
        super(GlobalEventExecutor.INSTANCE);
    }

    public ChannelGroupFuture writeAndFlushRandom(Object message) {
        final int size = super.size();
        if (size <= 0) {
            return super.writeAndFlush(message);
        }

        return super.writeAndFlush(message, new ChannelMatcher() {
            private int index = 0;
            private int matchedIndex = random.nextInt(size());

            @Override
            public boolean matches(Channel channel) {
                return matchedIndex == index++;
            }
        });
    }
}
