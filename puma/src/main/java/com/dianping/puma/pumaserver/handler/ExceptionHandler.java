package com.dianping.puma.pumaserver.handler;

import com.dianping.cat.Cat;
import com.dianping.puma.core.dto.ExceptionResponse;
import com.dianping.puma.core.util.ConvertHelper;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAckException;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAuthException;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;
import com.dianping.puma.pumaserver.exception.binlog.BinlogTargetException;
import com.dianping.puma.pumaserver.exception.client.ClientNotRegisterException;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public static final ExceptionHandler INSTANCE = new ExceptionHandler();

    private static final Map<Class<?>, HowToHandle> howToHandles = new HashMap<Class<?>, HowToHandle>();

    static {
        howToHandles.put(
                BinlogAuthException.class,
                new HowToHandle(UNAUTHORIZED, "puma binlog auth error.", true)
        );
        howToHandles.put(
                BinlogTargetException.class,
                new HowToHandle(INTERNAL_SERVER_ERROR, "puma binlog target error.", true)
        );
        howToHandles.put(
                BinlogChannelException.class,
                new HowToHandle(INTERNAL_SERVER_ERROR, "puma binlog channel error.", true)
        );
        howToHandles.put(
                BinlogAckException.class,
                new HowToHandle(INTERNAL_SERVER_ERROR, "puma binlog ack error.", true)
        );
        howToHandles.put(
                ClientNotRegisterException.class,
                new HowToHandle(INTERNAL_SERVER_ERROR, "client not register exception.", true)
        );
        howToHandles.put(
                RuntimeException.class,
                new HowToHandle(INTERNAL_SERVER_ERROR, "puma binlog server internal error.", true)
        );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            logger.error(cause.getMessage(), cause);
            Cat.logError(cause.getMessage(), cause);
        } else {
            logger.error(cause.getMessage(), cause);
            Cat.logError(cause.getMessage(), cause);

            // Handler server internal exceptions.
            HowToHandle howToHandle = howToHandles.get(cause.getClass());

            // All unclassified exceptions are treated as runtime exception.
            if (howToHandle == null) {
                howToHandle = howToHandles.get(RuntimeException.class);
            }

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1,
                    howToHandle.getStatus(),
                    Unpooled.wrappedBuffer(ConvertHelper.toBytes(new ExceptionResponse(howToHandle.getMsg() + cause.getMessage()))));

            ChannelFuture future = ctx.channel().writeAndFlush(response);

            if (howToHandle.isCloseChannel()) {
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        f.channel().close();
                    }
                });
            }
        }
    }

    static final class HowToHandle {

        private HttpResponseStatus status;

        private String msg;

        private boolean closeChannel;

        public HowToHandle(HttpResponseStatus status, String msg, boolean closeChannel) {
            this.status = status;
            this.msg = msg;
            this.closeChannel = closeChannel;
        }

        public HttpResponseStatus getStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }

        public boolean isCloseChannel() {
            return closeChannel;
        }
    }
}
