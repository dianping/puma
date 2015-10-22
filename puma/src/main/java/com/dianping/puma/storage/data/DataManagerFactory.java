package com.dianping.puma.storage.data;

import com.dianping.puma.storage.data.impl.SingleReadDataManager;

public class DataManagerFactory {

	private static final int readBufSizeByte = 64 * 1024 * 1024; // 64M.

	private static final int readAvgSizeByte = 16 * 1024 * 1024; // 64M.

	private static SingleReadDataManager newSingleReadDataManager(String filename) {
		return new SingleReadDataManager(filename, readBufSizeByte, readAvgSizeByte);
	}
}
