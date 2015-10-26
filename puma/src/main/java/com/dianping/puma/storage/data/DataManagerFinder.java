package com.dianping.puma.storage.data;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.filesystem.FileSystem;

import java.io.File;
import java.io.IOException;

public final class DataManagerFinder {

	public static SingleReadDataManager findMasterReadDataManager(String database, Sequence sequence)
			throws IOException {
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitMasterDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file);
	}

	public static SingleReadDataManager findSlaveReadDataManager(String database, Sequence sequence)
			throws IOException {
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitSlaveDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file);
	}

	public static SingleReadDataManager findNextMasterReadDataManager(String database, Sequence sequence)
			throws IOException {
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitNextMasterDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file);
	}

	public static SingleReadDataManager findNextSlaveReadDataManager(String database, Sequence sequence)
			throws IOException {
		String date = sequence.date();
		int number = sequence.getNumber();

		File file = FileSystem.visitNextSlaveDataFile(database, date, number);
		return file == null ? null : DataManagerFactory.newSingleReadDataManager(file);
	}

	public static SingleWriteDataManager findNextMasterWriteDataManager(String database) throws IOException {
		File file = FileSystem.nextMasterDataFile(database);
		String date = FileSystem.parseMasterDataDate(file);
		int number = FileSystem.parseMasterDataNumber(file);

		return file == null ? null : DataManagerFactory.newSingleWriteDataManager(file, date, number);
	}
}
