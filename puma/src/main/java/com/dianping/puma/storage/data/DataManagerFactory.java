package com.dianping.puma.storage.data;

public final class DataManagerFactory {

	private static final int READ_BUF_SIZE_BYTE = 64 * 1024 * 1024; // 64M.

	private static final int READ_AVG_SIZE_BYTE = 16 * 1024 * 1024; // 64M.

	private static final int WRITE_BUF_SIZE_BYTE = 16 * 1024 * 1024; // 64M.

	private static final int WRITE_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

	private DataManagerFactory() {
	}

	public static SingleReadDataManager newSingleReadDataManager(String filename) {
		return new SingleReadDataManager(filename, READ_BUF_SIZE_BYTE, READ_AVG_SIZE_BYTE);
	}

	public static SingleWriteDataManager newSingleWriteDataManager(String filename) {
		return new SingleWriteDataManager(filename, WRITE_BUF_SIZE_BYTE, WRITE_MAX_SIZE_BYTE);
	}
}
