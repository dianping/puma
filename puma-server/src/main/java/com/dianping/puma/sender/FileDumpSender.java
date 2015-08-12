/**
 * Project: puma-server
 * 
 * File Created at 2012-7-7
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
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
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
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

	private String binlogIndexBaseDir;

	private String masterStorageBaseDir;

	private String masterBucketFilePrefix;

	private String slaveStorageBaseDir;

	private String slaveBucketFilePrefix;

	private int maxMasterFileCount;

	private int maxMasterBucketLengthMB;

	private int maxSlaveBucketLengthMB;

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

				if (transactionBegin != null) {
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
		DefaultEventStorage storage = new DefaultEventStorage();
		storage.setName("storage-" + database);
		storage.setTaskName(taskName);
		storage.setCodec(codec);

		// File sender master storage.
		LocalFileDataBucketManager masterBucketIndex = new LocalFileDataBucketManager();
		masterBucketIndex.setBaseDir(masterStorageBaseDir + "/" + database);
		masterBucketIndex.setBucketFilePrefix(masterBucketFilePrefix);
		masterBucketIndex.setMaxBucketLengthMB(maxMasterBucketLengthMB);

		storage.setMasterBucketIndex(masterBucketIndex);

		// File sender slave storage.
		LocalFileDataBucketManager slaveBucketIndex = new LocalFileDataBucketManager();
		slaveBucketIndex.setBaseDir(slaveStorageBaseDir + "/" + database);
		slaveBucketIndex.setBucketFilePrefix(slaveBucketFilePrefix);
		slaveBucketIndex.setMaxBucketLengthMB(maxSlaveBucketLengthMB);

		storage.setSlaveBucketIndex(slaveBucketIndex);

		// Archive strategy.
		DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
		archiveStrategy.setServerName(taskName);
		archiveStrategy.setMaxMasterFileCount(maxMasterFileCount);
		storage.setArchiveStrategy(archiveStrategy);

		// Clean up strategy.
		DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
		cleanupStrategy.setPreservedDay(preservedDay);
		storage.setCleanupStrategy(cleanupStrategy);

		storage.setBinlogIndexBaseDir(binlogIndexBaseDir + database);

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

	public void setBinlogIndexBaseDir(String binlogIndexBaseDir) {
		this.binlogIndexBaseDir = binlogIndexBaseDir;
	}

	public void setMasterStorageBaseDir(String masterStorageBaseDir) {
		this.masterStorageBaseDir = masterStorageBaseDir;
	}

	public void setSlaveStorageBaseDir(String slaveStorageBaseDir) {
		this.slaveStorageBaseDir = slaveStorageBaseDir;
	}

	public void setMaxMasterBucketLengthMB(int maxMasterBucketLengthMB) {
		this.maxMasterBucketLengthMB = maxMasterBucketLengthMB;
	}

	public void setMaxSlaveBucketLengthMB(int maxSlaveBucketLengthMB) {
		this.maxSlaveBucketLengthMB = maxSlaveBucketLengthMB;
	}

	public void setSlaveBucketFilePrefix(String slaveBucketFilePrefix) {
		this.slaveBucketFilePrefix = slaveBucketFilePrefix;
	}

	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	public void setMasterBucketFilePrefix(String masterBucketFilePrefix) {
		this.masterBucketFilePrefix = masterBucketFilePrefix;
	}

	public void setMaxMasterFileCount(int maxMasterFileCount) {
		this.maxMasterFileCount = maxMasterFileCount;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	@Override
	public EventStorage getStorage(String database) {
		return storages.get(database);
	}
}
