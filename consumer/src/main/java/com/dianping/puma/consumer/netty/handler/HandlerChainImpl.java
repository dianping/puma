package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.netty.channel.ChannelHandler;

import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class HandlerChainImpl extends AbstractPumaLifeCycle implements HandlerChain {

    private List<Pair<String, ChannelHandler>> namedChannelHandlers = Lists.newArrayList();

    @Override
    public Iterator<Pair<String, ChannelHandler>> iterator() {
        return namedChannelHandlers.iterator();
    }
}
