package com.dianping.puma.storage.index.impl;

import com.dianping.puma.storage.index.IndexManagerFinder;
import com.dianping.puma.storage.index.ReadIndexManager;
import com.dianping.puma.storage.index.WriteIndexManager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class SeriesIndexManagerFinder implements IndexManagerFinder {

	private static final String L1_INDEX_PREFIX = "l1index";

	private static final String L1_INDEX_SUFFIX = ".l1idx";

	private static final String L2_INDEX_PREFIX = "bucket-";

	private static final String L2_INDEX_SUFFIX = ".l2idx";

	private static final String L2_DATE_FORMAT = "yyyyMMdd";

	private String database;

	private String l1IndexBaseDir;

	private String l2IndexBaseDir;

	public SeriesIndexManagerFinder(String database, String l1IndexBaseDir, String l2IndexBaseDir) {
		this.database = database;
		this.l1IndexBaseDir = l1IndexBaseDir;
		this.l2IndexBaseDir = l2IndexBaseDir;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public ReadIndexManager<L1IndexKey, L1IndexValue> findL1ReadIndexManager() throws IOException {
		File file = loadL1Index();

		return new L1SingleReadIndexManager(file.getName());
	}

	@Override
	public ReadIndexManager<L2IndexKey, L2IndexValue> findL2ReadIndexManager(L1IndexValue l1IndexValue)
			throws IOException {
		String date = l1IndexValue.getSequence().date();
		int number = l1IndexValue.getSequence().getNumber();

		File file = loadL2Index(date, number);
		return new L2SingleReadIndexManager(file.getName());
	}

	@Override
	public WriteIndexManager<L1IndexKey, L1IndexValue> findL1WriteIndexManager() throws IOException {
		File file = loadL1Index();

		return new L1SingleWriteIndexManager(file.getName());
	}

	@Override
	public WriteIndexManager<L2IndexKey, L2IndexValue> findNextL2WriteIndexManager()
			throws IOException {
		return null;
//		String date = l1IndexValue.getSequence().date();
//		int number = l1IndexValue.getSequence().getNumber();
//
//		File file = newL2IndexFile(date, number);
//		return new SingleWriteIndexManager<L2IndexKey, L2IndexValue>(file.getName());
	}

	protected File loadL1Index() throws IOException {
		File baseDir = new File(l1IndexBaseDir, database);
		if (!baseDir.exists()) {
			throw new IOException("failed to load l1 index.");
		}

		File[] files = baseDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return matchL1IndexFile(filename);
			}
		});

		if (files == null || files.length == 0) {
			throw new IOException("failed to load l2 index, no index file found.");
		}

		if (files.length >= 2) {
			throw new IOException("failed to load l2 index, multiple index file found.");
		}

		return files[0];
	}

	protected boolean matchL1IndexFile(String filename) {
		return filename != null && filename.startsWith(L1_INDEX_PREFIX)
				&& filename.endsWith(L1_INDEX_SUFFIX);
	}

	protected File loadL2Index(final String date, final int number) throws IOException {
		File baseDir = new File(l2IndexBaseDir, database);
		if (!baseDir.exists()) {
			throw new IOException("failed to load l2 index, base dir not found.");
		}

		File[] dateDirs = baseDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return matchL2IndexFolder(date, filename);
			}
		});

		if (dateDirs == null || dateDirs.length == 0) {
			throw new IOException("failed to load l2 index, date folder not found.");
		}

		if (dateDirs.length >= 2) {
			throw new IOException("failed to load l2 index, multiple date folder found.");
		}

		File dateDir = dateDirs[0];

		File[] files = dateDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return matchL2IndexFile(number, filename);
			}
		});

		if (files == null || files.length == 0) {
			throw new IOException("failed to load l2 index, index file not found.");
		}

		if (files.length >= 2) {
			throw new IOException("failed to load l2 index, multiple index file found.");
		}

		return files[0];
	}

	protected boolean matchL2IndexFolder(String date, String folderName) {
		return date.equals(folderName);
	}

	protected boolean matchL2IndexFile(int number, String filename) {
		return (L2_INDEX_PREFIX + number + L2_INDEX_SUFFIX).equals(filename);
	}
//
//	protected File loadNextL2Index(String oriDate, int oriNumber) throws IOException {
//		String date = new SimpleDateFormat(L2_DATE_FORMAT).format(oriDate);
//		if (date.compareTo(oriDate) > 0) {
//			return newL2IndexFile(date, 0);
//		} else {
//			return newL2IndexFile(date, oriNumber + 1);
//		}
//	}
//
//	protected File newL2IndexFile(String date, int number) throws IOException {
//		File baseDir = new File(l2IndexBaseDir, database);
//		File dateDir = new File(baseDir, date);
//
//		if (!dateDir.exists()) {
//			dateDir.mkdirs();
//		}
//
//		File file = new File(dateDir, L2_INDEX_PREFIX + number + L2_INDEX_PREFIX);
//		file.createNewFile();
//		return file;
//	}
//
//	protected int maxIndexNumber(String dateDir) {
//
//	}
}
