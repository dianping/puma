package com.dianping.puma.storage.data;

import java.io.File;

public final class DataManagerFactory {

    private static final int READ_BUF_SIZE_BYTE = 1024 * 1024; // 1M.

    private static final int READ_AVG_SIZE_BYTE = 16 * 1024 * 1024; // 64M.

    private static final int WRITE_BUF_SIZE_BYTE = 1024 * 1024; // 1M.

    private static final int WRITE_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

    private DataManagerFactory() {
    }

    public static SingleReadDataManager newSingleReadDataManager(File file) {
        return new SingleReadDataManager(file, READ_BUF_SIZE_BYTE, READ_AVG_SIZE_BYTE);
    }

    public static SingleWriteDataManager newSingleWriteDataManager(File file, String date, int number) {
        return new SingleWriteDataManager(file, date, number, WRITE_BUF_SIZE_BYTE, WRITE_MAX_SIZE_BYTE);
    }
}
