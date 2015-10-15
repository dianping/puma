package com.dianping.puma.storage.data.manage;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.biz.DataManagerFinder;
import com.dianping.puma.storage.data.bucket.ReadDataBucket;

import java.io.EOFException;
import java.io.IOException;

public class SingleReadDataManager extends AbstractLifeCycle implements ReadDataManager {

	private final String masterBaseDir;

	private final String slaveBaseDir;

	private final String database;

	private DataManagerFinder master;

	private DataManagerFinder slave;

	private ReadDataBucket readDataBucket;

	public SingleReadDataManager(String masterBaseDir, String slaveBaseDir, String database) {
		this.masterBaseDir = masterBaseDir;
		this.slaveBaseDir = slaveBaseDir;
		this.database = database;
	}

	@Override
	public void doStart() {
		//master = DataBucketManagerFactory.newDataBucketManager(masterBaseDir, database);
		master.start();

		//slave = DataBucketManagerFactory.newDataBucketManager(slaveBaseDir, database);
		slave.start();
	}

	@Override
	public void doStop() {
		master.stop();
		slave.stop();

		readDataBucket.stop();
	}

	@Override public void open(Object dataKey) throws IOException {

	}

	public void open(Sequence sequence) throws IOException {
		checkStop();

		// 先找到sequence对应的bucket文件，再移动相应的偏移量。
		long offset = sequence.getOffset();

		// 先在slave中查找
		readDataBucket = slave.findReadDataBucket(sequence);
		if (readDataBucket == null) {
			// 后在master中查找
			readDataBucket = master.findReadDataBucket(sequence);
			if (readDataBucket == null) {
				throw new IOException(
						String.format("failed to open read data bucket for database `%s`.", database));
			}
		}

		// 启动bucket
		readDataBucket.start();
		readDataBucket.skip(offset);
	}

	@Override
	public byte[] next() throws IOException {
		checkStop();

		while (true) {
			try {
				return readDataBucket.next();
			} catch (EOFException eof) {
				// 读完一个bucket后，关闭当前bucket，打开下一个bucket
				Sequence sequence = readDataBucket.sequence();
				close(readDataBucket);
				openNext(sequence);
			}
		}
	}

	protected void close(ReadDataBucket bucket) {
		bucket.stop();
	}

	protected void skip(ReadDataBucket readDataBucket, long offset) throws IOException {
		readDataBucket.skip(offset);
	}

	protected void openNext(Sequence sequence) throws IOException {
		checkStop();

		// 先在slave中查找,slave中的日期必定早于master
		readDataBucket = slave.findNextReadDataBucket(sequence);
		if (readDataBucket == null) {
			// 后在master中查找
			readDataBucket = master.findNextReadDataBucket(sequence);
			if (readDataBucket == null) {
				throw new IOException(
						String.format("failed to open next read data bucket for database `%s`.", database));
			}
		}

		// 启动bucket
		readDataBucket.start();
	}
}
