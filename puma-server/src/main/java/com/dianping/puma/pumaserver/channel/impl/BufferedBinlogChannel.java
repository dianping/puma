package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class BufferedBinlogChannel implements BinlogChannel {

    private volatile boolean stopped = true;

    private ExecutorService executorService;

    private EventChannel eventChannel;

    private TaskContainer taskContainer;

    private final AtomicReference<BinlogGetRequest> request = new AtomicReference<BinlogGetRequest>();

    @Override
    public void init(
            String targetName,
            long dbServerId,
            long sc,
            BinlogInfo binlogInfo,
            long timestamp,
            String database,
            List<String> tables,
            boolean dml,
            boolean ddl,
            boolean transaction
    ) throws BinlogChannelException {

        EventStorage eventStorage = taskContainer.getTaskStorage(targetName);

        if (eventStorage == null) {
            throw new BinlogChannelException("find event storage failure, not exist.");
        }

        try {
            eventChannel = eventStorage.getChannel(
                    sc,
                    dbServerId,
                    binlogInfo.getBinlogFile(),
                    binlogInfo.getBinlogPosition(),
                    timestamp
            );
            eventChannel.withDatabase(database);
            eventChannel.withTables(tables.toArray(new String[tables.size()]));
            eventChannel.withDml(dml);
            eventChannel.withDdl(ddl);
            eventChannel.withTransaction(transaction);
            eventChannel.open();

            executorService = Executors.newFixedThreadPool(1);
            executorService.execute(extractTask);

        } catch (Exception e) {
            throw new BinlogChannelException("find event storage failure", e.getCause());
        }
    }

    private Runnable extractTask = new Runnable() {
        @Override
        public void run() {
            List<Event> results = new ArrayList<Event>();

            while (!stopped && !Thread.interrupted()) {
                BinlogGetRequest req = request.get();

                if (req == null && results.size() > 1000) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        stopped = true;
                    }
                    continue;
                }


                Event binlogEvent;
                try {
                    binlogEvent = eventChannel.next();
                } catch (Exception e) {
                    binlogEvent = new ServerErrorEvent("get binlog event from storage failure.", e.getCause());
                }


                if (binlogEvent != null) {
                    results.add(binlogEvent);
                }

                boolean needSend = false;
                if (req != null && results.size() > req.getBatchSize()) {
                    needSend = true;
                }

                if (!needSend && req != null &&
                        req.getTimeout() > 0 &&
                        req.getStartTime() + req.getTimeUnit().toMillis(req.getTimeout()) < System.currentTimeMillis()) {
                    needSend = true;
                }

                if (needSend) {
                    if (req.getChannel().isActive()) {
                        BinlogGetResponse response = new BinlogGetResponse();
                        BinlogMessage message = new BinlogMessage();

                        Iterator<Event> iterator = results.iterator();
                        while (iterator.hasNext() && message.getBinlogEvents().size() < req.getBatchSize()) {
                            message.addBinlogEvents(iterator.next());
                            iterator.remove();
                        }

                        req.getChannel().writeAndFlush(response.setBinlogMessage(message));
                        request.set(null);
                        //todo: auto ack
                    }
                }
            }
        }
    };

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
}
