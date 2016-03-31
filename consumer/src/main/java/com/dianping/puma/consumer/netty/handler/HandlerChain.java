package com.dianping.puma.consumer.netty.handler;

import com.dianping.puma.common.PumaLifeCycle;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.netty.channel.ChannelHandler;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public interface HandlerChain extends Iterable<Pair<String, ChannelHandler>>, PumaLifeCycle {
}
