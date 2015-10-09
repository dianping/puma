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

		// 先在slave中查找
		readDataBucket = slave.findReadDataBucket(sequence);
		if (readDataBucket != null) {
			readDataBucket.skip(offset);
		} else {
			// 后在master中查找
			readDataBucket = master.findReadDataBucket(sequence);
			if (readDataBucket != null) {
				readDataBucket.skip(offset);
			} else {
				throw new IOException(
						String.format("failed to open read data bucket for database `%s`", database));
			}
		}
	}

	@Override
	public byte[] next() throws IOException {
		checkStop();

		while (true) {
			try {
				return readDataBucket.next();
			} catch (EOFException eof) {
				// 读完一个bucket后，打开下一个bucket
				openNext(readDataBucket.sequence());
			}
		}
	}

	protected void skip(ReadDataBucket readDataBucket, long offset) throws IOException {
		readDataBucket.skip(offset);
	}

	protected void openNext(Sequence sequence) throws IOException {
		checkStop();

		// 先在slave中查找
		readDataBucket = slave.findNextReadDataBucket(sequence);
		if (readDataBucket == null) {
			// 后在master中查找
			readDataBucket = master.findNextReadDataBucket(sequence);
			if (readDataBucket == null) {
				throw new IOException(
						String.format("failed to open next read data bucket for database `%s`.", database));
			}
		}
	}
}
