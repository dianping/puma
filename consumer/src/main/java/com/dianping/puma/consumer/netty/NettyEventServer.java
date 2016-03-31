package com.dianping.puma.consumer.netty;

import com.dianping.puma.consumer.AbstractPumaEventServer;
import com.dianping.puma.consumer.exception.PumaEventServerException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaotian.li on 16/3/11.
 * Email: lixiaotian07@gmail.com
 */
public class NettyEventServer extends AbstractPumaEventServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int port = 4040;

    private int connectTimeoutInSecond = 10;

    private boolean epoll = true;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private ServerBootstrap bootstrap;

    @Override
    public void start() {
        super.start();

        bossGroup = epoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        workGroup = epoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup);
        bootstrap.channel(epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

        bootstrap.childHandler(new NettyEventServerInitializer());

        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInSecond * 1000);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        logger.info("Start to bootstrap puma netty event server on port[{}].", port);

        ChannelFuture channelFuture = bootstrap.bind(port).awaitUninterruptibly();
        if (channelFuture.isSuccess()) {
            logger.info("Success to bootstrap puma netty event server on port[{}]", port);
        } else {
            throw new PumaEventServerException("Failed to bootstrap puma netty " +
                    "event server on port[%s].", port, channelFuture.cause());
        }
    }

    @Override
    public void stop() {
        super.stop();

        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setConnectTimeoutInSecond(int connectTimeoutInSecond) {
        this.connectTimeoutInSecond = connectTimeoutInSecond;
    }

    public void setEpoll(boolean epoll) {
        this.epoll = epoll;
    }
}
