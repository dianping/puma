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

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.dianping.cat.Cat;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
import com.dianping.puma.storage.conf.GlobalStorageConfig;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

/**
 *
 * @author Leo Liang
 *
 */
public class FileDumpSender extends AbstractSender {
	private Map<String, DefaultEventStorage> storages = new ConcurrentHashMap<String, DefaultEventStorage>();

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
				DefaultEventStorage eventStorage = storages.get(database);

				if (eventStorage == null) {
					eventStorage = buildEventStorage(database);
					storages.put(database, eventStorage);
				}

				boolean isTransactionBegin = false;

				if (event instanceof RowChangedEvent) {
					isTransactionBegin = ((RowChangedEvent) event).isTransactionBegin();
				}

				if (transactionBegin != null && !isTransactionBegin) {
					eventStorage.store(transactionBegin);
					transactionBegin = null;
				}

				eventStorage.store(event);
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
		} catch (StorageException e) {
			throw new SenderException("FileDumpSender.doSend failed.", e);
		}
	}

	private DefaultEventStorage buildEventStorage(String database) throws StorageLifeCycleException {
		// File sender master storage.
		LocalFileDataBucketManager masterBucketIndex = new LocalFileDataBucketManager();
		masterBucketIndex.setBaseDir(GlobalStorageConfig.masterStorageBaseDir + "/" + database);
		masterBucketIndex.setBucketFilePrefix(GlobalStorageConfig.masterBucketFilePrefix);
		masterBucketIndex.setMaxBucketLengthMB(GlobalStorageConfig.maxMasterBucketLengthMB);

		// File sender slave storage.
		LocalFileDataBucketManager slaveBucketIndex = new LocalFileDataBucketManager();
		slaveBucketIndex.setBaseDir(GlobalStorageConfig.slaveStorageBaseDir + "/" + database);
		slaveBucketIndex.setBucketFilePrefix(GlobalStorageConfig.slaveBucketFilePrefix);
		slaveBucketIndex.setMaxBucketLengthMB(GlobalStorageConfig.maxMasterBucketLengthMB);

		// Archive strategy.
		DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
		archiveStrategy.setServerName(taskName);
		archiveStrategy.setMaxMasterFileCount(GlobalStorageConfig.maxMasterFileCount);

		// Clean up strategy.
		DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
		cleanupStrategy.setPreservedDay(preservedDay);

		DefaultEventStorage storage = new DefaultEventStorage();
		storage.setName("storage-" + database);
		storage.setTaskName(taskName);
		storage.setCodec(codec);
		storage.setMasterBucketIndex(masterBucketIndex);
		storage.setSlaveBucketIndex(slaveBucketIndex);
		storage.setArchiveStrategy(archiveStrategy);
		storage.setCleanupStrategy(cleanupStrategy);
		storage.setBinlogIndexBaseDir(GlobalStorageConfig.binlogIndexBaseDir + "/" + database);

		storage.start();
		return storage;
	}

	public void setStorageEventFilterChain(EventFilterChain storageEventFilterChain) {
		this.storageEventFilterChain = storageEventFilterChain;
	}

	public void setStorages(Map<String, DefaultEventStorage> storages) {
		this.storages = storages;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	@Override
	public EventStorage getStorage(String database) {
		try {
			DefaultEventStorage eventStorage = storages.get(database);
			if (eventStorage == null) {
				eventStorage = buildEventStorage(database);
				storages.put(database, eventStorage);
			}
			return eventStorage;
		} catch (Exception e) {
			return null;
		}
	}

	public void setCodec(RawEventCodec rawCodec) {
		this.codec = rawCodec;
	}
}
