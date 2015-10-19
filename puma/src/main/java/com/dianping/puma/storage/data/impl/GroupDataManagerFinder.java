package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.DataManagerFinder;
import com.dianping.puma.storage.data.ReadDataManager;
import com.dianping.puma.storage.data.WriteDataManager;
import com.dianping.puma.storage.bucket.LocalFileReadBucket;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class GroupDataManagerFinder extends AbstractLifeCycle implements DataManagerFinder {

	private final String bucketPrefix = "Bucket-";

	private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	private String baseDir;

	private String masterBaseDir;

	private String slaveBaseDir;

	private String database;

	private File rootDir;

	private TreeMap<Sequence, File> index = new TreeMap<Sequence, File>(new Comparator<Sequence>() {
		@Override public int compare(Sequence sequence0, Sequence sequence1) {
			long seq0 = sequence0.longValue();
			long seq1 = sequence1.longValue();
			if (seq0 < seq1) {
				return -1;
			} else if (seq0 == seq1) {
				return 0;
			} else {
				return 1;
			}
		}
	});

	public GroupDataManagerFinder(String database, String masterBaseDir, String slaveBaseDir) {
		this.database = database;
		this.masterBaseDir = masterBaseDir;
		this.slaveBaseDir = slaveBaseDir;
	}

	public GroupDataManagerFinder(String baseDir, String database) {
		this.baseDir = baseDir;
		this.database = database;
		this.rootDir = new File(baseDir, database);
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public File rootDir() {
		return rootDir;
	}

//	@Override
//	public ReadDataBucket findReadDataBucket(Sequence sequence) throws IOException {
//		loadIndex();
//		Sequence sequenceNoOffset = new Sequence(sequence).clearOffset();
//		File file = index.get(sequenceNoOffset);
//		if (file == null) {
//			return null;
//		}
//		return new LocalFileReadBucket(sequenceNoOffset, file);
//	}
//
//	@Override
//	public ReadDataBucket findNextReadDataBucket(Sequence sequence) throws IOException {
//		loadIndex();
//		Map.Entry<Sequence, File> entry = index.higherEntry(sequence);
//		if (entry == null) {
//			return null;
//		}
//		return new LocalFileReadBucket(entry.getKey(), entry.getValue());
//	}

	protected void loadIndex() throws IOException {
		index.clear();

		File dir = new File(baseDir, database);
		if (!dir.exists()) {
			throw new IOException(String.format("failed to load index for database `%s`.", database));
		}

		File[] dateDirs = dir.listFiles();
		if (dateDirs != null) {
			for (File dateDir: dateDirs) {
				File[] buckets = dateDir.listFiles();
				if (buckets != null) {
					for (File bucket: buckets) {
						try {
							Sequence sequence = buildSequence(dateDir, bucket);
							index.put(sequence, bucket);
						} catch (IOException ignore) {
						}
					}
				}
			}
		}
	}

	protected Sequence buildSequence(File dateDir, File bucketFile) throws IOException {
		try {
			// 解析日期文件夹，标准格式为：20150925
			String dateString = dateDir.getName();
			if (dateFormat.parse(dateString) == null) {
				throw new IOException("illegal date directory name.");
			}
			int dateInteger = Integer.valueOf(dateString);

			// 解析Bucket文件名字，标准格式为：Bucket-0
			String numberString = bucketFile.getName();
			if (!StringUtils.startsWith(numberString, bucketPrefix)) {
				throw new IOException("illegal bucket file name.");
			}
			numberString = StringUtils.substringAfter(numberString, bucketPrefix);
			int numberInteger = Integer.valueOf(numberString);

			return new Sequence(dateInteger, numberInteger);
		} catch (Throwable t) {
			throw new IOException("failed to build sequence.", t);
		}
	}

	@Override public ReadDataManager<DataKeyImpl, DataValueImpl> findReadDataBucket(DataKeyImpl dataKey)
			throws IOException {
		return null;
	}

	@Override public ReadDataManager<DataKeyImpl, DataValueImpl> findNextReadDataBucket(DataKeyImpl dataKey)
			throws IOException {
		return null;
	}

	@Override public WriteDataManager<DataKeyImpl, DataValueImpl> genNextWriteDataBucket() throws IOException {
		return null;
	}
}
