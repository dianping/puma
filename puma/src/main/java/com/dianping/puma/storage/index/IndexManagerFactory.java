package com.dianping.puma.storage.index;

public final class IndexManagerFactory {

	private static int l1ReadBufSizeByte = 64 * 1024; // 64K.

	private static int l1ReadAvgSizeByte = 64 * 1024; // 64K.

	private static int l1WriteBufSizeByte = 64 * 1024; // 64K.

	private static int l1WriteMaxSizeByte = 64 * 1024; // 64K.

	private static int l2ReadBufSizeByte = 64 * 1024; // 64K.

	private static int l2ReadAvgSizeByte = 64 * 1024; // 64K.

	private static int l2WriteBufSizeByte = 64 * 1024; // 64K.

	private static int l2WriteMaxSizeByte = 64 * 1024; // 64K.

	public static L1SingleReadIndexManager newL1SingleReadIndexManager(String filename) {
		return new L1SingleReadIndexManager(filename, l1ReadBufSizeByte, l1ReadAvgSizeByte);
	}

	public static L1SingleReadIndexManager newL1SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		return new L1SingleReadIndexManager(filename, bufSizeByte, avgSizeByte);
	}

	public static L1SingleWriteIndexManager newL1SingleWriteIndexManager(String filename) {
		return new L1SingleWriteIndexManager(filename, l1WriteBufSizeByte, l1WriteMaxSizeByte);
	}

	public static L1SingleWriteIndexManager newL1SingleWriteIndexManager(String filename, int bufSizeByte, int maxSizeByte) {
		return new L1SingleWriteIndexManager(filename, bufSizeByte, maxSizeByte);
	}

	public static L2SingleReadIndexManager newL2SingleReadIndexManager(String filename) {
		return new L2SingleReadIndexManager(filename, l2ReadBufSizeByte, l2ReadAvgSizeByte);
	}

	public static L2SingleReadIndexManager newL2SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		return new L2SingleReadIndexManager(filename, bufSizeByte, avgSizeByte);
	}

	public static L2SingleWriteIndexManager newL2SingleWriteIndexManager(String filename) {
		return new L2SingleWriteIndexManager(filename, l2WriteBufSizeByte, l2WriteMaxSizeByte);
	}

	public static L2SingleWriteIndexManager newL2SingleWriteIndexManager(String filename, int bufSizeByte, int maxSizeByte) {
		return new L2SingleWriteIndexManager(filename, bufSizeByte, maxSizeByte);
	}
}
