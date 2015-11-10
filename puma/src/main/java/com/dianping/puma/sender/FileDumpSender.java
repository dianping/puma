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

import java.io.IOException;
import java.util.Map;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.channel.ChannelFactory;
import com.dianping.puma.storage.channel.ReadChannel;
import com.dianping.puma.storage.channel.WriteChannel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.dianping.cat.Cat;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.filter.EventFilterChain;

/**
 *
 * @author Leo Liang
 *
 */
public class FileDumpSender extends AbstractSender {
	private Map<String, ReadChannel> readChannels = new ConcurrentHashMap<String, ReadChannel>();

	private Map<String, WriteChannel> writeChannels = new ConcurrentHashMap<String, WriteChannel>();

	private ChangedEvent transactionBegin;

	private EventFilterChain storageEventFilterChain;

	private String taskName;

	private int preservedDay;

	private EventCodec codec;

	@Override
	public void start() throws Exception {
		super.start();
	}

	@Override
	public void stop() throws Exception {
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

//		// Archive strategy.
//		DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
//		archiveStrategy.setServerName(taskName);
//		archiveStrategy.setMaxMasterFileCount(GlobalStorageConfig.MAX_MASTER_FILE_COUNT);
//
//		// Clean up strategy.
//		DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
//		cleanupStrategy.setPreservedDay(preservedDay);
//
//		DefaultEventStorage storage = new DefaultEventStorage();
//		storage.setName("storage-" + database);
//		storage.setTaskName(taskName);
//		storage.setCodec(codec);
//		storage.setArchiveStrategy(archiveStrategy);
//		storage.setCleanupStrategy(cleanupStrategy);
//		storage.setBinlogIndexBaseDir(GlobalStorageConfig.BINLOG_INDEX_BASE_DIR + "/" + database);
//
//		storage.start();
//		return storage;
	}

	public void setStorageEventFilterChain(EventFilterChain storageEventFilterChain) {
		this.storageEventFilterChain = storageEventFilterChain;
	}

	public void setReadChannels(Map<String, ReadChannel> readChannels) {
		this.readChannels = readChannels;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	@Override
	public ReadChannel getStorage(String database) {
		try {
			ReadChannel readChannel = readChannels.get(database);
//			if (readChannel == null) {
//				readChannel = buildEventStorage(database);
//				readChannels.put(database, readChannel);
//			}
			return readChannel;
		} catch (Exception e) {
			return null;
		}
	}

	public void setCodec(RawEventCodec rawCodec) {
		this.codec = rawCodec;
	}
}
