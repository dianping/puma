package com.dianping.puma.server.netty;

import com.dianping.puma.server.AbstractPumaEventServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class NettyEventServer extends AbstractPumaEventServer {

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private ServerBootstrap serverBootstrap;

    private boolean epoll;

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }
}
