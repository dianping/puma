package com.dianping.puma.storage.bucket;

public final class BucketFactory {

	private static final int lineReadBucketBufSizeByte = 1024; // 1K.

	private static final int lineReadBucketAvgSizeByte = 1024; // 1K.

	private static final int lineWriteBucketBufSizeByte = 1024; // 1K.

	private static final int lineWriteBucketMaxSizeByte = 1024 * 1024 * 1024; // 1G.

	public static LineReadBucket newLineReadBucket(String filename) {
		return new LineReadBucket(filename, lineReadBucketBufSizeByte, lineReadBucketAvgSizeByte);
	}

	public static LineReadBucket newLineReadBucket(String filename, int bufSizeByte, int avgBufSize) {
		return new LineReadBucket(filename, bufSizeByte, avgBufSize);
	}

	public static LineWriteBucket newLineWriteBucket(String filename) {
		return new LineWriteBucket(filename, lineWriteBucketBufSizeByte, lineWriteBucketMaxSizeByte);
	}

	public static LineWriteBucket newLineWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		return new LineWriteBucket(filename, bufSizeByte, maxSizeByte);
	}
}
