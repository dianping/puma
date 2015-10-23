package com.dianping.puma.storage.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class FileSystem {

	private static String l1IndexPrefix = "l1Index";

	private static String l1IndexSuffix = ".l1idx";

	private static String l2IndexPrefix = "bucket-";

	private static String l2IndexSuffix = ".l2idx";

	private static String datePattern = "yyyyMMdd";

	private static File l1IndexDir;

	private static File l2IndexDir;

	private static File masterDataDir;

	private static File slaveDataDir;

	private static DateFormat dateFormat = new SimpleDateFormat(datePattern);
	
	public static File getL1IndexDir() {
		return null;
	}

	public static File getL2IndexDir() {
		return null;
	}

	public static File getMasterDataDir() {
		return null;
	}

	public static File getSlaveDataDir() {
		return null;
	}

	public static File visitL1IndexFile(String database) {
		File databaseDir = new File(l1IndexDir, database);
		File l1Index = new File(databaseDir, genL1IndexName());
		return l1Index.isFile() && l1Index.canRead() && l1Index.canWrite() ? l1Index : null;
	}

	public static File[] visitL2IndexDateDirs() {
		File[] databaseDirs = l2IndexDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		if (databaseDirs == null) {
			return new File[0];
		}

		List<File> l2Indices = new ArrayList<File>();
		for (File databaseDir : databaseDirs) {
			String database = databaseDir.getName();
			File[] files = visitL2IndexDateDirs(database);
			l2Indices.addAll(Arrays.asList(files));
		}

		return l2Indices.toArray(new File[]{});
	}

	public static File[] visitL2IndexDateDirs(String database) {
		File databaseDir = new File(l2IndexDir, database);
		File[] l2Indices = databaseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		if (l2Indices == null) {
			return new File[0];
		}
		return l2Indices;
	}

	public static File visitL2IndexFile(String database, int date, int number) {
		File databaseDir = new File(l2IndexDir, database);
		File dateDir = new File(databaseDir, String.valueOf(date));
		File l2Index = new File(dateDir, genL2IndexName(number));
		return l2Index.isFile() && l2Index.canRead() && l2Index.canWrite() ? l2Index : null;
	}

	public static File nextL2IndexFile(String database) {
		File[] dateDirs = visitL2IndexDateDirs(database);
		File maxDateDir = maxDateDir(dateDirs);

		String maxDate = maxDateDir.getName();
		String curDate = dateFormat.format(new Date());
		if (curDate.compareTo(maxDate) > 0) {
			maxDate = curDate;
			maxDateDir = createDateDir(database, maxDate);
		}

		File maxL2IndexFile = maxL2IndexFile(database, maxDate);
		int number = maxL2IndexFile == null ? 0 : parseL2IndexFileNumber(maxL2IndexFile);
		return createL2IndexFile(database, maxDate, number);
	}

	public static File[] visitMasterDataDateDirs() {
		return null;
	}

	public static File[] visitMasterDataDateDirs(String database) {
		return null;
	}

	public static File visitMasterDataFile(String database, String date, int number) {
		return null;
	}

	public static File visitNextMasterDataFile(String database, String date, int number) {
		return null;
	}

	public static File nextMasterDataFile(String database) {
		return null;
	}

	public static File[] visitSlaveDataDateDirs() {
		return null;
	}

	public static File[] visitSlaveDataDateDirs(String database) {
		return null;
	}

	public static File visitSlaveDataFile(String database, String date, int number) {
		return null;
	}

	public static File visitNextSlaveDataFile(String database, String date, int number) {
		return null;
	}

	public static File nextSlaveDataFile(String database) {
		return null;
	}

	public static Date parseDateDir(File dateDir) {
		return null;
	}

	public static File createDateDir(File baseDir, Date date) {
		return null;
	}

	protected static File createDateDir(String database, String date) {
		return null;
	}

	protected static File createL2IndexFile(String database, String date, int number) {
		return null;
	}

	protected static File maxDateDir(File[] dateDirs) {
		return null;
	}

	protected static File maxL2IndexFile(String database, String date) {
		return null;
	}

	protected static int parseL2IndexFileNumber(File l2IndexFile) {
		return 0;
	}

	protected static String genL1IndexName() {
		return l1IndexPrefix + l1IndexSuffix;
	}

	protected static String genL2IndexName(int number) {
		return l2IndexPrefix + number + l2IndexSuffix;
	}
}
