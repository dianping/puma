package com.dianping.puma.storage.data;

import com.dianping.puma.storage.data.impl.SingleReadDataManager;
import com.dianping.puma.storage.data.impl.SingleWriteDataManager;

public class DataManagerFactory {

	private static final int readBufSizeByte = 64 * 1024 * 1024; // 64M.

	private static final int readAvgSizeByte = 16 * 1024 * 1024; // 64M.

	private static final int writeBufSizeByte = 64 * 1024 * 1024; // 64M.

	private static final int writeMaxSizeByte = 1024 * 1024 * 1024; // 1G.

	private static SingleReadDataManager newSingleReadDataManager(String filename) {
		return new SingleReadDataManager(filename, readBufSizeByte, readAvgSizeByte);
	}

	private static SingleWriteDataManager newSingleWriteDataManager(String filename) {
		return new SingleWriteDataManager(filename, writeBufSizeByte, writeMaxSizeByte);
	}
}
