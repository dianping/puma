/**
 * Project: puma-server
 * <p/>
 * File Created at 2012-7-7
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.sender;

import com.dianping.cat.Cat;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.storage.channel.ChannelFactory;
import com.dianping.puma.storage.channel.WriteChannel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.io.IOException;
import java.util.Map;

/**
 * @author Leo Liang
 */
public class FileDumpSender extends AbstractSender {
    private Map<String, WriteChannel> writeChannels = new ConcurrentHashMap<String, WriteChannel>();

    private ChangedEvent transactionBegin;

    private EventFilterChain storageEventFilterChain;

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        for (WriteChannel channel : writeChannels.values()) {
            channel.stop();
        }
        super.stop();
    }

    @Override
    protected void doSend(ChangedEvent event, PumaContext context) throws SenderException {
        // Storage filter.
        storageEventFilterChain.reset();
        if (!storageEventFilterChain.doNext(event)) {
            return;
        }

        try {
            String database = event.getDatabase();

            if (database != null && database.length() > 0) {
                WriteChannel writeChannel = this.writeChannels.get(database);

                if (writeChannel == null) {
                    writeChannel = buildEventStorage(database);
                    this.writeChannels.put(database, writeChannel);
                }

                boolean isTransactionBegin = false;

                if (event instanceof RowChangedEvent) {
                    isTransactionBegin = ((RowChangedEvent) event).isTransactionBegin();
                }

                if (transactionBegin != null && !isTransactionBegin) {
                    //readChannel.store(transactionBegin);
                    transactionBegin = null;
                }


                writeChannel.append(event);
            } else {
                if (event instanceof RowChangedEvent) {
                    if (((RowChangedEvent) event).isTransactionBegin()) {
                        transactionBegin = event;
                    } else {
                        Cat.logEvent("Puma", "RowChangeEvent-Has-No-Database");
                        LOG.error(String.format("RowChangeEvent[%s] has no database", event.toString()));
                    }
                } else {
                    Cat.logEvent("Puma", "ChangeEvent-Has-No-Database");
                    LOG.error(String.format("ChangeEvent[%s] has no database", event.toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WriteChannel buildEventStorage(String database) {
        WriteChannel writeChannel = ChannelFactory.newWriteChannel(database);
        writeChannel.start();
        return writeChannel;
    }

    public void setStorageEventFilterChain(EventFilterChain storageEventFilterChain) {
        this.storageEventFilterChain = storageEventFilterChain;
    }
}
