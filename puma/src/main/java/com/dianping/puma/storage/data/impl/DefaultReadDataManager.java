package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.DataBucketManager;
import com.dianping.puma.storage.data.ReadDataBucket;
import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.data.factory.DataBucketManagerFactory;

import java.io.EOFException;
import java.io.IOException;

public class DefaultReadDataManager extends AbstractLifeCycle implements ReadDataManager {

	private final String database;

	private Sequence sequence;

	private DataBucketManager masterDataBucketManager;

	private DataBucketManager slaveDataBucketManager;

	private ReadDataBucket readDataBucket;

	public DefaultReadDataManager(String database) {
		this.database = database;
	}

	@Override
	public void doStart() {
		masterDataBucketManager = DataBucketManagerFactory.newMasterDataBucketManager(database);
		masterDataBucketManager.start();

		slaveDataBucketManager = DataBucketManagerFactory.newMasterDataBucketManager(database);
		slaveDataBucketManager.start();
	}

	@Override
	public void doStop() {
		masterDataBucketManager.stop();
		slaveDataBucketManager.stop();
	}

	@Override
	public void open(Sequence sequence) throws IOException {
		if (isStopped()) {
			throw new RuntimeException("failed to open when read data manager is stopped.");
		}

		readDataBucket = slaveDataBucketManager.findReadDataBucket(sequence);
		if (readDataBucket == null) {
			readDataBucket = masterDataBucketManager.findReadDataBucket(sequence);
			if (readDataBucket == null) {
				throw new IOException("failed to get data bucket.");
			}
		}

		// @todo: set sequence.
	}

	@Override
	public byte[] next() throws IOException {
		while (true) {
			try {
				byte[] data = readDataBucket.next();
				sequence.addOffset(data.length);
				return data;
			} catch (EOFException eof) {
				openNext(sequence);
			}
		}
	}

	protected void openNext(Sequence sequence) throws IOException {
		if (isStopped()) {
			throw new RuntimeException("failed to open next when read data manager is stopped.");
		}

		readDataBucket = slaveDataBucketManager.findNextReadDataBucket(sequence);
		if (readDataBucket == null) {
			readDataBucket = masterDataBucketManager.findNextReadDataBucket(sequence);
			if (readDataBucket == null) {
				throw new IOException("failed to get data bucket.");
			}
		}

		// @todo: set sequence.
	}
}
