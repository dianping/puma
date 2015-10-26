package com.dianping.puma.storage.index;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.filesystem.FileSystem;

import java.io.File;
import java.io.IOException;

public final class SeriesIndexManagerFinder {

	public static L1SingleReadIndexManager findL1ReadIndexManager(String database) throws IOException {
		File file = FileSystem.visitL1IndexFile(database);
		return IndexManagerFactory.newL1SingleReadIndexManager(file);
	}

	public static L2SingleReadIndexManager findL2ReadIndexManager(String database, Sequence sequence)
			throws IOException {
		String date = sequence.date();
		int number = sequence.getNumber();
		File file = FileSystem.visitL2IndexFile(database, date, number);
		return IndexManagerFactory.newL2SingleReadIndexManager(file);
	}

	public static L1SingleWriteIndexManager findL1WriteIndexManager(String database) throws IOException {
		File file = FileSystem.visitL1IndexFile(database);
		if (file == null) {
			file = FileSystem.nextL1IndexFile(database);
		}
		return IndexManagerFactory.newL1SingleWriteIndexManager(file);
	}

	public static L2SingleWriteIndexManager findNextL2WriteIndexManager(String database) throws IOException {
		File file = FileSystem.nextL2IndexFile(database);
		return IndexManagerFactory.newL2SingleWriteIndexManager(file);
	}
}
