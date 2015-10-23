package com.dianping.puma.storage.index;

public final class IndexManagerFactory {

	private static final int L1_READ_BUF_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L1_READ_AVG_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L1_WRITE_BUF_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L1_WRITE_MAX_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L2_READ_BUF_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L2_READ_AVG_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L2_WRITE_BUF_SIZE_BYTE = 64 * 1024; // 64K.

	private static final int L2_WRITE_MAX_SIZE_BYTE = 64 * 1024; // 64K.

	private IndexManagerFactory() {
	}

	public static L1SingleReadIndexManager newL1SingleReadIndexManager(String filename) {
		return new L1SingleReadIndexManager(filename, L1_READ_BUF_SIZE_BYTE, L1_READ_AVG_SIZE_BYTE);
	}

	public static L1SingleReadIndexManager newL1SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		return new L1SingleReadIndexManager(filename, bufSizeByte, avgSizeByte);
	}

	public static L1SingleWriteIndexManager newL1SingleWriteIndexManager(String filename) {
		return new L1SingleWriteIndexManager(filename, L1_WRITE_BUF_SIZE_BYTE, L1_WRITE_MAX_SIZE_BYTE);
	}

	public static L1SingleWriteIndexManager newL1SingleWriteIndexManager(String filename, int bufSizeByte, int maxSizeByte) {
		return new L1SingleWriteIndexManager(filename, bufSizeByte, maxSizeByte);
	}

	public static L2SingleReadIndexManager newL2SingleReadIndexManager(String filename) {
		return new L2SingleReadIndexManager(filename, L2_READ_BUF_SIZE_BYTE, L2_READ_AVG_SIZE_BYTE);
	}

	public static L2SingleReadIndexManager newL2SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		return new L2SingleReadIndexManager(filename, bufSizeByte, avgSizeByte);
	}

	public static L2SingleWriteIndexManager newL2SingleWriteIndexManager(String filename) {
		return new L2SingleWriteIndexManager(filename, L2_WRITE_BUF_SIZE_BYTE, L2_WRITE_MAX_SIZE_BYTE);
	}

	public static L2SingleWriteIndexManager newL2SingleWriteIndexManager(String filename, int bufSizeByte, int maxSizeByte) {
		return new L2SingleWriteIndexManager(filename, bufSizeByte, maxSizeByte);
	}
}
