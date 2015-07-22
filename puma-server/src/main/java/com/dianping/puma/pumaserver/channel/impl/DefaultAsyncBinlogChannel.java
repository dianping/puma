package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultAsyncBinlogChannel implements AsyncBinlogChannel {

    private final static Logger logger = LoggerFactory.getLogger(DefaultAsyncBinlogChannel.class);

    private volatile boolean stopped = false;

    protected static final ExecutorService executorService = Executors.newCachedThreadPool();

    private volatile EventChannel eventChannel;

    private TaskContainer taskContainer;

    private final AtomicReference<BinlogGetRequest> request = new AtomicReference<BinlogGetRequest>();

    @Override
    public void init(
            long sc,
            BinlogInfo binlogInfo,
            long timestamp,
            String database,
            List<String> tables,
            boolean dml,
            boolean ddl,
            boolean transaction
    ) throws BinlogChannelException {
        EventStorage eventStorage = taskContainer.getTaskStorage(database);

        if (eventStorage == null) {
            throw new BinlogChannelException("find event storage failure, not exist.");
        }

        try {
            eventChannel = eventStorage.getChannel(
                    sc,
                    binlogInfo == null ? 0 : binlogInfo.getServerId(),
                    binlogInfo == null ? null : binlogInfo.getBinlogFile(),
                    binlogInfo == null ? 0 : binlogInfo.getBinlogPosition(),
                    timestamp
            );
            eventChannel.withDatabase(database);
            eventChannel.withTables(tables.toArray(new String[tables.size()]));
            eventChannel.withDml(dml);
            eventChannel.withDdl(ddl);
            eventChannel.withTransaction(transaction);
            eventChannel.open();

            executorService.execute(new AsyncTask(new WeakReference<DefaultAsyncBinlogChannel>(this)));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BinlogChannelException("find event storage failure", e.getCause());
        }
    }

    @Override
    public void destroy() {
        stopped = true;
        executorService.shutdown();
        eventChannel.close();
    }

    @Override
    public boolean addRequest(BinlogGetRequest request) {
        return this.request.compareAndSet(null, request);
    }

    public void setTaskContainer(TaskContainer taskContainer) {
        this.taskContainer = taskContainer;
    }

    static class AsyncTask implements Runnable {
        private final static Logger logger = LoggerFactory.getLogger(AsyncTask.class);

        private final WeakReference<DefaultAsyncBinlogChannel> parent;

        public AsyncTask(WeakReference<DefaultAsyncBinlogChannel> parent) {
            this.parent = parent;
        }

        private DefaultAsyncBinlogChannel getParent() throws InterruptedException {
            DefaultAsyncBinlogChannel channel = parent.get();
            if (channel == null) {
                logger.warn("Parent has be GCed. Please check your code to call destroy.");
                Thread.currentThread().interrupt();
                throw new InterruptedException();
            }
            return channel;
        }

        @Override
        public void run() {
            List<Event> results = new ArrayList<Event>();

            try {
                while (!getParent().stopped && !Thread.currentThread().isInterrupted()) {
                    BinlogGetRequest req = getParent().request.get();
                    if (req != null && !req.getChannel().isActive()) {
                        getParent().request.set(null);
                        req = null;
                    }

                    if (req == null && results.size() > 1000) {
                        Thread.sleep(5);
                        continue;
                    }

                    Event binlogEvent;
                    try {
                        binlogEvent = getParent().eventChannel.next(false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        binlogEvent = new ServerErrorEvent("get binlog event from storage failure.", e.getCause());
                    }

                    if (binlogEvent != null) {
                        results.add(binlogEvent);
                    }

                    boolean needSend = false;
                    if (req != null && (results.size() >= req.getBatchSize() || req.isTimeout())) {
                        needSend = true;
                    }

                    if (needSend) {
                        getParent().request.set(null);

                        BinlogGetResponse response = new BinlogGetResponse();
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

                        req.getChannel().writeAndFlush(response.setBinlogMessage(message));
                        //todo: auto ack

                        SystemStatusManager.updateClientSendBinlogInfo(req.getClientName(), lastBinlogInfo);
                    }

                    if (binlogEvent == null) {
                        Thread.sleep(5);
                    }
                }
            } catch (InterruptedException e) {
                logger.info("AsyncTask has be Interrupted");
            }
        }
    }
}
