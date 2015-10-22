package com.dianping.puma.storage.data;

public final class DataManagerFactory {

	private static final int readBufSizeByte = 64 * 1024 * 1024; // 64M.

	private static final int readAvgSizeByte = 16 * 1024 * 1024; // 64M.

	private static final int writeBufSizeByte = 16 * 1024 * 1024; // 64M.

	private static final int writeMaxSizeByte = 1024 * 1024 * 1024; // 1G.

	public static SingleReadDataManager newSingleReadDataManager(String filename) {
		return new SingleReadDataManager(filename, readBufSizeByte, readAvgSizeByte);
	}

	public static SingleWriteDataManager newSingleWriteDataManager(String filename) {
		return new SingleWriteDataManager(filename, writeBufSizeByte, writeMaxSizeByte);
	}
}
