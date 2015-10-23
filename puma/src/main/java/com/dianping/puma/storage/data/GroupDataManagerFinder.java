package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.filesystem.FileSystem;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.TreeMap;

public final class GroupDataManagerFinder extends AbstractLifeCycle implements DataManagerFinder {

	private String database;

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

	public GroupDataManagerFinder(String database) {
		this.database = database;
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public ReadDataManager<DataKeyImpl, DataValueImpl> findMasterReadDataManager(DataKeyImpl dataKey)
			throws IOException {
		Sequence sequence = dataKey.getSequence();
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitMasterDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file.getAbsolutePath());
	}

	@Override
	public ReadDataManager<DataKeyImpl, DataValueImpl> findSlaveReadDataManager(DataKeyImpl dataKey)
			throws IOException {
		Sequence sequence = dataKey.getSequence();
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitSlaveDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file.getAbsolutePath());
	}

	@Override
	public ReadDataManager<DataKeyImpl, DataValueImpl> findNextMasterReadDataManager(DataKeyImpl dataKey)
			throws IOException {
		Sequence sequence = dataKey.getSequence();
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitNextMasterDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file.getAbsolutePath());
	}

	@Override
	public ReadDataManager<DataKeyImpl, DataValueImpl> findNextSlaveReadDataManager(DataKeyImpl dataKey)
			throws IOException {
		Sequence sequence = dataKey.getSequence();
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitNextSlaveDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file.getAbsolutePath());
	}

	@Override
	public WriteDataManager<DataKeyImpl, DataValueImpl> findNextMasterWriteDataManager() throws IOException {
		File file = FileSystem.nextMasterDataFile(database);
		return file == null ? null : DataManagerFactory.newSingleWriteDataManager(file.getAbsolutePath());
	}

	@Override
	public WriteDataManager<DataKeyImpl, DataValueImpl> findNextSlaveWriteDataManager() throws IOException {
		File file = FileSystem.nextSlaveDataFile(database);
		return file == null ? null : DataManagerFactory.newSingleWriteDataManager(file.getAbsolutePath());
	}

	//	protected Sequence buildSequence(File dateDir, File bucketFile) throws IOException {
//		try {
//			// 解析日期文件夹，标准格式为：20150925
//			String dateString = dateDir.getName();
//			if (dateFormat.parse(dateString) == null) {
//				throw new IOException("illegal date directory name.");
//			}
//			int dateInteger = Integer.valueOf(dateString);
//
//			// 解析Bucket文件名字，标准格式为：Bucket-0
//			String numberString = bucketFile.getName();
//			if (!StringUtils.startsWith(numberString, bucketPrefix)) {
//				throw new IOException("illegal bucket file name.");
//			}
//			numberString = StringUtils.substringAfter(numberString, bucketPrefix);
//			int numberInteger = Integer.valueOf(numberString);
//
//			return new Sequence(dateInteger, numberInteger);
//		} catch (Throwable t) {
//			throw new IOException("failed to build sequence.", t);
//		}
//	}
}
