package com.dianping.puma.pumaserver.server;

import com.dianping.cat.Cat;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 11/21/14
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class TcpServer {
    private static Logger logger = LoggerFactory.getLogger(TcpServer.class);

    private static final boolean needToUseEpoll = SystemUtils.IS_OS_LINUX;

    private volatile EventLoopGroup bossGroup;

    private volatile EventLoopGroup workerGroup;

    private volatile ServerBootstrap bootstrap;

    private final ServerConfig config;

    private volatile boolean closed = false;

    public TcpServer(ServerConfig config) {
        this.config = config;
    }

    public void close() {
        closed = true;

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        logger.info("Stopped Tcp Server: " + config.getPort());
        Cat.logEvent("Server.Stop", "Tcp: " + config.getPort());
    }

    public void init() {
        if (closed) {
            return;
        }

        bossGroup = needToUseEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        workerGroup = needToUseEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);

        bootstrap.channel(needToUseEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                Map<String, ChannelHandler> handlers = config.getHandlerFactory().getHandlers();
                for (Map.Entry<String, ChannelHandler> entry : handlers.entrySet()) {
                    pipeline.addLast(entry.getKey(), entry.getValue());
                }
            }
        });

        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);


        doBind();
    }

    protected void doBind() {
        if (closed) {
            return;
        }

        bootstrap.bind(config.getPort()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    logger.info("Started Tcp Server: " + config.getPort());
                    Cat.logEvent("Server.Started", "Tcp: " + config.getPort());
                } else {
                    String msg = "Started Tcp Server Failed: " + config.getPort();
                    logger.error(msg, f.cause());
                    Cat.logError(msg, f.cause());

                    f.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doBind();
                        }
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
    }
}
