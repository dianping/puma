package com.dianping.puma.storage.bucket;

public final class BucketFactory {

	private static final int lineReadBucketBufSizeByte = 1024; // 1K.

	private static final int lineReadBucketAvgSizeByte = 1024; // 1K.

	private static final int lineWriteBucketBufSizeByte = 1024; // 1K.

	private static final int lineWriteBucketMaxSizeByte = 1024 * 1024 * 1024; // 1G.

	private static final int lengthReadBucketBufSizeByte = 16 * 1024 * 1024; // 16M.

	private static final int lengthReadBucketAvgSizeByte = 10 * 1024; // 10K.

	private static final int lengthWriteBucketBufSizeByte = 16 * 1024 * 1024; // 16M.

	private static final int lengthWriteBucketMaxSizeByte = 1024 * 1024 * 1024; // 1G.

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

	public static LengthReadBucket newLengthReadBucket(String filename) {
		return new LengthReadBucket(filename, lengthReadBucketBufSizeByte, lengthReadBucketAvgSizeByte);
	}

	public static LengthReadBucket newLengthReadBucket(String filename, int bufSizeByte, int avgSizeByte) {
		return new LengthReadBucket(filename, bufSizeByte, avgSizeByte);
	}

	public static LengthWriteBucket newLengthWriteBucket(String filename) {
		return new LengthWriteBucket(filename, lengthWriteBucketBufSizeByte, lengthWriteBucketMaxSizeByte);
	}

	public static LengthWriteBucket newLengthWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		return new LengthWriteBucket(filename, bufSizeByte, maxSizeByte);
	}
}
