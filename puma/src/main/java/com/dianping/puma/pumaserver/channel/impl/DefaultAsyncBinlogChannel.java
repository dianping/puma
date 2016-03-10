package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.cat.Cat;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.ExceptionResponse;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.ConvertHelper;
import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.channel.ChannelFactory;
import com.dianping.puma.storage.channel.ReadChannel;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class DefaultAsyncBinlogChannel implements AsyncBinlogChannel {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAsyncBinlogChannel.class);

    protected static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private final String clientName;

    private volatile String database;

    private volatile boolean stopped = false;

    private volatile ReadChannel readChannel;

    private final BlockingQueue<BinlogGetRequest> requests = new LinkedBlockingQueue<BinlogGetRequest>(5);

    public DefaultAsyncBinlogChannel(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void init(
            BinlogInfo binlogInfo,
            String database,
            List<String> tables,
            boolean dml,
            boolean ddl,
            boolean transaction
    ) throws BinlogChannelException {
        try {
            this.database = database;
            this.readChannel = initChannel(binlogInfo, tables, dml, ddl, transaction);
            THREAD_POOL.execute(new AsyncTask(new WeakReference<DefaultAsyncBinlogChannel>(this)));
        } catch (Exception e) {
            throw new BinlogChannelException(clientName + " find event storage failure!", e);
        }
    }

    protected ReadChannel initChannel(BinlogInfo binlogInfo, List<String> tables,
                                      boolean dml, boolean ddl, boolean transaction) throws IOException {
        ReadChannel readChannel = ChannelFactory.newReadChannel(database, tables, dml, ddl, transaction);
        readChannel.start();

        if (binlogInfo == null) {
            readChannel.openLatest();
        } else {
            readChannel.open(binlogInfo);
        }

        return readChannel;
    }

    @Override
    public void destroy() {
        stopped = true;

        if (readChannel != null) {
            readChannel.stop();
        }
    }

    @Override
    public boolean addRequest(BinlogGetRequest request) {
        return requests.offer(request);
    }

    static class AsyncTask implements Runnable {
        private static final Logger LOG = LoggerFactory.getLogger(AsyncTask.class);

        private static final int CACHE_SIZE = 1000;

        private static final int EMPTY_SLEEP_TIME = 1;

        private final WeakReference<DefaultAsyncBinlogChannel> parent;

        public AsyncTask(WeakReference<DefaultAsyncBinlogChannel> parent) {
            this.parent = parent;
        }

        private boolean threadNameHasSet = false;

        private Exception lastException = null;

        @Override
        public void run() {
            List<Event> results = new ArrayList<Event>();

            try {
                BinlogGetRequest req = null;

                while (!getParent().stopped && !Thread.currentThread().isInterrupted()) {
                    req = getBinlogGetRequest(results, req);
                    boolean needSend = isNeedSend(results, req);
                    if (needSend) {
                        if (results.size() == 0 && lastException != null) {
                            FullHttpResponse response = new DefaultFullHttpResponse(
                                    HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                    Unpooled.wrappedBuffer(ConvertHelper.toBytes(new ExceptionResponse("read event failed!" + lastException.getMessage()))));
                            req.getChannel().writeAndFlush(response);
                        } else {
                            req.getChannel().writeAndFlush(buildBinlogGetResponse(results, req));
                        }
                        req = null;
                    }

                    if (req != null || results.size() < CACHE_SIZE) {
                        Event binlogEvent = getEvent();
                        if (binlogEvent != null) {
                            results.add(binlogEvent);
                            continue;
                        }
                    }

                    Thread.sleep(EMPTY_SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.info("AsyncTask has be Interrupted");
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                Cat.logError(e.getMessage(), e);
            }
        }

        protected BinlogGetResponse buildBinlogGetResponse(List<Event> results, BinlogGetRequest req) {
            BinlogGetResponse response = new BinlogGetResponse();
            response.setClientName(parent.get().clientName);
            response.setBinlogGetRequest(req);
            BinlogMessage message = new BinlogMessage();
            BinlogInfo lastBinlogInfo = null;

            Iterator<Event> iterator = results.iterator();
            while (iterator.hasNext() && message.getBinlogEvents().size() < req.getBatchSize()) {
                Event event = iterator.next();
                message.addBinlogEvents(event);
                if (event.getBinlogInfo() != null) {
                    lastBinlogInfo = event.getBinlogInfo();
                }
                iterator.remove();
            }
            response.setBinlogMessage(message);

            SystemStatusManager.updateClientSendBinlogInfo(req.getClientName(), lastBinlogInfo);
            SystemStatusManager.addClientFetchQps(req.getClientName(), message.getBinlogEvents().size());
            return response;
        }

        protected boolean isNeedSend(List<Event> results, BinlogGetRequest req) {
            return req != null && (results.size() >= req.getBatchSize() || req.isTimeout());
        }

        protected Event getEvent() {
            try {
                ChangedEvent event = getParent().readChannel.next();
                SystemStatusManager.updateClientStorageMode(getParent().clientName, getParent().readChannel.getStorageMode());
                lastException = null;
                return event;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                lastException = e;
                return null;
            }
        }

        protected BinlogGetRequest getBinlogGetRequest(List<Event> results, BinlogGetRequest req)
                throws InterruptedException {
            BinlogGetRequest request = req;

            if (!(request != null && request.getChannel().isActive())) {
                request = getParent().requests.poll();
            }

            if (request == null && results.size() >= CACHE_SIZE) {
                while (!getParent().stopped && !Thread.currentThread().isInterrupted() && request == null) {
                    request = getParent().requests.poll(1, TimeUnit.SECONDS);
                }
            }

            if (request != null && !threadNameHasSet) {
                threadNameHasSet = true;
                Thread.currentThread().setName("DefaultAsyncBinlogChannel-" + request.getClientName());
            }

            return request;
        }

        protected DefaultAsyncBinlogChannel getParent() throws InterruptedException {
            DefaultAsyncBinlogChannel channel = parent.get();
            if (channel == null) {
                LOG.warn("Parent has be GCed. Please check your code to call destroy.");
                Thread.currentThread().interrupt();
                throw new InterruptedException();
            }
            return channel;
        }
    }
}
