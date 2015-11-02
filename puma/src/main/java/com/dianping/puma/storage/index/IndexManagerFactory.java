package com.dianping.puma.storage.index;

import java.io.File;

public final class IndexManagerFactory {

    private static final int L1_READ_BUF_SIZE_BYTE = 64 * 1024; // 64K.

    private static final int L1_READ_AVG_SIZE_BYTE = 64 * 1024; // 64K.

    private static final int L1_WRITE_BUF_SIZE_BYTE = 64 * 1024; // 64K.

    private static final int L1_WRITE_MAX_SIZE_BYTE = 1024 * 1024; // 1M.

    private static final int L2_READ_BUF_SIZE_BYTE = 64 * 1024; // 64K.

    private static final int L2_READ_AVG_SIZE_BYTE = 64 * 1024; // 64K.

    private static final int L2_WRITE_BUF_SIZE_BYTE = 64 * 1024; // 64K.

    private static final int L2_WRITE_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

    private IndexManagerFactory() {
    }

    public static L1SingleReadIndexManager newL1SingleReadIndexManager(File file) {
        return new L1SingleReadIndexManager(file, L1_READ_BUF_SIZE_BYTE, L1_READ_AVG_SIZE_BYTE);
    }

    public static L1SingleReadIndexManager newL1SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
        return new L1SingleReadIndexManager(file, bufSizeByte, avgSizeByte);
    }

    public static L1SingleWriteIndexManager newL1SingleWriteIndexManager(File file) {
        return new L1SingleWriteIndexManager(file, L1_WRITE_BUF_SIZE_BYTE, L1_WRITE_MAX_SIZE_BYTE);
    }

    public static L1SingleWriteIndexManager newL1SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
        return new L1SingleWriteIndexManager(file, bufSizeByte, maxSizeByte);
    }

    public static L2SingleReadIndexManager newL2SingleReadIndexManager(File file) {
        return new L2SingleReadIndexManager(file, L2_READ_BUF_SIZE_BYTE, L2_READ_AVG_SIZE_BYTE);
    }

    public static L2SingleReadIndexManager newL2SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
        return new L2SingleReadIndexManager(file, bufSizeByte, avgSizeByte);
    }

    public static L2SingleWriteIndexManager newL2SingleWriteIndexManager(File file, String date, int number) {
        return new L2SingleWriteIndexManager(file, date, number, L2_WRITE_BUF_SIZE_BYTE, L2_WRITE_MAX_SIZE_BYTE);
    }

    public static L2SingleWriteIndexManager newL2SingleWriteIndexManager(File file, String date, int number, int bufSizeByte, int maxSizeByte) {
        return new L2SingleWriteIndexManager(file, date, number, bufSizeByte, maxSizeByte);
    }
}
