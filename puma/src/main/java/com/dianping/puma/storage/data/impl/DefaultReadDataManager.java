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

	private final String masterBaseDir;

	private final String slaveBaseDir;

	private final String database;

	private DataBucketManager master;

	private DataBucketManager slave;

	private ReadDataBucket readDataBucket;

	private Sequence sequence;

	public DefaultReadDataManager(String masterBaseDir, String slaveBaseDir, String database) {
		this.masterBaseDir = masterBaseDir;
		this.slaveBaseDir = slaveBaseDir;
		this.database = database;
	}

	@Override
	public void doStart() {
		master = DataBucketManagerFactory.newDataBucketManager(masterBaseDir, database);
		master.start();

		slave = DataBucketManagerFactory.newDataBucketManager(slaveBaseDir, database);
		slave.start();
	}

	@Override
	public void doStop() {
		master.stop();
		slave.stop();
	}

	@Override
	public void open(Sequence sequence) throws IOException {
		checkStop();

		// 先找到sequence对应的bucket文件，再移动相应的偏移量。
		long offset = sequence.getOffset();

		readDataBucket = slave.findReadDataBucket(sequence);
		if (readDataBucket != null) {
			readDataBucket.skip(offset);
		} else {
			readDataBucket = master.findReadDataBucket(sequence);
			if (readDataBucket != null) {
				readDataBucket.skip(offset);
			} else {
				throw new IOException(
						String.format("failed to get read data bucket for database `%s`", database));
			}
		}
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

	protected void skip(ReadDataBucket readDataBucket, long offset) throws IOException {
		readDataBucket.skip(offset);
	}

	protected void openNext(Sequence sequence) throws IOException {
		checkStop();

		readDataBucket = slave.findNextReadDataBucket(sequence);
		if (readDataBucket == null) {
			readDataBucket = master.findNextReadDataBucket(sequence);
			if (readDataBucket == null) {
				throw new IOException(
						String.format("failed to get next read data bucket for database `%s`.", database));
			}
		}

		// @todo: set sequence.
	}
}
