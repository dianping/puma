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

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.exception.StorageException;

/**
 * 
 * @author Leo Liang
 * 
 */
public class FileDumpSender extends AbstractSender {
	private EventStorage storage;

	private Map<String, DefaultEventStorage> storages = new ConcurrentHashMap<String, DefaultEventStorage>();

	private ChangedEvent transactionBegin;

	private EventFilterChain storageEventFilterChain;
	
	private String taskName;

	private String binlogIndexBaseDir;
	
	private String masterStorageBaseDir;
	
	private String slaveStorageBaseDir;
	
	private int maxMasterBucketLengthMB;
	
	private int maxSlaveBucketLengthMB;
	
	private String slaveBucketFilePrefix;
	
	
	
	private EventCodec codec;

	/**
	 * @param storage
	 *           the storage to set
	 */
	public void setStorage(EventStorage storage) {
		this.storage = storage;
	}

	@Override
	public EventStorage getStorage() {
		return storage;
	}

	@Override
	public void start() throws Exception {
		storage.start();
		super.start();
	}

	@Override
	public void stop() throws Exception {
		storage.stop();
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
//			String database = event.getDatabase();
//
//			if (database != null && database.length() > 0) {
//				DefaultEventStorage eventStorage = storages.get(database);
//
//				if (eventStorage == null) {
//					eventStorage = new DefaultEventStorage();
//
//					eventStorage.setName(database);
//					eventStorage.setTaskName(taskName);
//
//				}
//
//				if (transactionBegin != null) {
//					eventStorage.store(transactionBegin);
//					transactionBegin = null;
//				}
//
//				eventStorage.store(event);
//			} else {
//				if (event instanceof RowChangedEvent) {
//					if (((RowChangedEvent) event).isTransactionBegin()) {
//						transactionBegin = event;
//					} else {
//
//					}
//				} else {
//
//				}
//			}

			 storage.store(event);
		} catch (StorageException e) {
			throw new SenderException("FileDumpSender.doSend failed.", e);
		}
	}

	public void setStorageEventFilterChain(EventFilterChain storageEventFilterChain) {
		this.storageEventFilterChain = storageEventFilterChain;
	}
}
