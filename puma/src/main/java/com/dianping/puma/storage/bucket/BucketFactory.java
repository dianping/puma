package com.dianping.puma.storage.bucket;

public final class BucketFactory {

	private static final int lineWriteBucketBufSizeByte = 1024; // 1K.

	private static final int lineWriteBucketMaxSizeByte = 1024 * 1024 * 1024; // 1G.

	public static LineWriteBucket newLineWriteBucket(String filename) {
		return new LineWriteBucket(filename, lineWriteBucketBufSizeByte, lineWriteBucketMaxSizeByte);
	}

	public static LineWriteBucket newLineWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		return new LineWriteBucket(filename, bufSizeByte, maxSizeByte);
	}
}
