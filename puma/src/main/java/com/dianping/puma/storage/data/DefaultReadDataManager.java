package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.EOFException;
import java.io.IOException;

public class DefaultReadDataManager extends AbstractLifeCycle implements ReadDataManager {

	private final String database;

	private Sequence sequence;

	private DataBucketManager masterDataBucketManager;

	private DataBucketManager slaveDataBucketManager;

	private DataBucket dataBucket;

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
		if (checkStop()) {
			throw new RuntimeException("failed to open when read data manager is stopped.");
		}

		dataBucket = slaveDataBucketManager.findReadDataBucket(sequence);
		if (dataBucket == null) {
			dataBucket = masterDataBucketManager.findReadDataBucket(sequence);
			if (dataBucket == null) {
				throw new IOException("failed to get data bucket.");
			}
		}

		// @todo: set sequence.
	}

	@Override
	public byte[] next() throws IOException {
		while (true) {
			try {
				byte[] data = dataBucket.getNext();
				sequence.addOffset(data.length);
				return data;
			} catch (EOFException eof) {
				openNext(sequence);
			}
		}
	}

	protected void openNext(Sequence sequence) throws IOException {
		if (checkStop()) {
			throw new RuntimeException("failed to open next when read data manager is stopped.");
		}

		dataBucket = slaveDataBucketManager.findNextReadDataBucket(sequence);
		if (dataBucket == null) {
			dataBucket = masterDataBucketManager.findNextReadDataBucket(sequence);
			if (dataBucket == null) {
				throw new IOException("failed to get data bucket.");
			}
		}

		// @todo: set sequence.
	}
}
