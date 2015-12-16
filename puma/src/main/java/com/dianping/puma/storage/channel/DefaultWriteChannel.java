package com.dianping.puma.storage.channel;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.cache.CachedGroupWriteDataManager;
import com.dianping.puma.storage.data.WriteDataManager;
import com.dianping.puma.storage.index.SeriesWriteIndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DefaultWriteChannel extends AbstractLifeCycle implements WriteChannel {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultWriteChannel.class);

    private String database;

    private SeriesWriteIndexManager writeIndexManager;

    private WriteDataManager<Sequence, ChangedEvent> writeDataManager;

    private Thread thread;

    protected DefaultWriteChannel(String database) {
        this.database = database;
    }

    @Override
    protected synchronized void doStart() {
        writeIndexManager = new SeriesWriteIndexManager(database);
        writeIndexManager.start();

        writeDataManager = new CachedGroupWriteDataManager(database);
        writeDataManager.start();

        thread = new Thread(new FlushTask());
        thread.setName("flush-" + database);
        thread.start();
    }

    @Override
    protected synchronized void doStop() {
        if (writeIndexManager != null) {
            writeIndexManager.stop();
        }

        if (writeDataManager != null) {
            writeDataManager.stop();
        }

        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void append(ChangedEvent binlogEvent) throws IOException {
        checkStop();

        BinlogInfo binlogInfo = binlogEvent.getBinlogInfo();

        Sequence sequence = writeDataManager.append(binlogEvent);
        writeIndexManager.append(binlogInfo, sequence);
    }

    @Override
    public void flush() throws IOException {
        checkStop();

        writeIndexManager.flush();
        writeDataManager.flush();
    }

    private class FlushTask implements Runnable {
        @Override
        public void run() {
            while (!isStopped() && !Thread.currentThread().isInterrupted()) {
                try {
                    flush();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        flush();
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception e) {
                    LOG.error("Flush failed!", e);
                }
            }
        }
    }
}
