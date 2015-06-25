package com.dianping.puma.core.netty.client;

import com.dianping.cat.Cat;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TcpClient {
    private final static boolean needToUseEpoll = SystemUtils.IS_OS_LINUX;

    private static Logger logger = LoggerFactory.getLogger(TcpClient.class);

    private final ClientConfig config;

    private volatile EventLoopGroup workerGroup;

    private volatile Bootstrap bootstrap;

    private volatile boolean closed = false;

    public TcpClient(ClientConfig config) {
        this.config = config;
    }

    public void close() {
        closed = true;
        workerGroup.shutdownGracefully();

        logger.info("Stopped Tcp Client: " + getServerInfo());
        Cat.logEvent("Client.Stop", "Tcp: " + getServerInfo());
    }

    public void init() {
        if (closed) {
            return;
        }

        workerGroup = needToUseEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(needToUseEpoll ? EpollSocketChannel.class : NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        super.channelInactive(ctx);

                        ctx.channel().eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                doConnect();
                            }
                        }, 5, TimeUnit.SECONDS);
                    }
                });

                if (config.getHandlerFactory() != null) {
                    Map<String, ChannelHandler> handlers = config.getHandlerFactory().getHandlers();
                    for (Map.Entry<String, ChannelHandler> entry : handlers.entrySet()) {
                        pipeline.addLast(entry.getKey(), entry.getValue());
                    }
                }
            }
        });

        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        doConnect();
    }

    private void doConnect() {
        if (closed) {
            return;
        }

        ChannelFuture future;
        if (config.getLocalPort() != 0) {
            future = bootstrap.connect(new InetSocketAddress(config.getRemoteIp(), config.getRemotePort()),
                    new InetSocketAddress("0.0.0.0", config.getLocalPort()));
        } else {
            future = bootstrap.connect(new InetSocketAddress(config.getRemoteIp(), config.getRemotePort()));
        }

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    logger.info("Started Tcp Client: " + getServerInfo());
                    Cat.logEvent("Client.Started", "Tcp: " + getServerInfo());
                } else {
                    String msg = "Started Tcp Client Failed: " + getServerInfo();
                    logger.error(msg, f.cause());
                    Cat.logError(msg, f.cause());
                    f.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
    }

    private String getServerInfo() {
        return String.format("RemoteIp=%s RemotePort=%d LocalPort=%d",
                config.getRemoteIp(),
                config.getRemotePort(),
                config.getLocalPort());
    }
}
