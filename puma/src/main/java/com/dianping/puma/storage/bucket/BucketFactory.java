package com.dianping.puma.storage.bucket;

public final class BucketFactory {

	private static final int LINE_READ_BUCKET_BUF_SIZE_BYTE = 1024; // 1K.

	private static final int LINE_READ_BUCKET_AVG_SIZE_BYTE = 1024; // 1K.

	private static final int LINE_WRITE_BUCKET_BUF_SIZE_BYTE = 1024; // 1K.

	private static final int LINE_WRITE_BUCKET_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

	private static final int LENGTH_READ_BUCKET_BUF_SIZE_BYTE = 16 * 1024 * 1024; // 16M.

	private static final int LENGTH_READ_BUCKET_AVG_SIZE_BYTE = 10 * 1024; // 10K.

	private static final int LENGTH_WRITE_BUCKET_BUF_SIZE_BYTE = 16 * 1024 * 1024; // 16M.

	private static final int LENGTH_WRITE_BUCKET_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

	private BucketFactory() {
	}

	public static LineReadBucket newLineReadBucket(String filename) {
		return new LineReadBucket(filename, LINE_READ_BUCKET_BUF_SIZE_BYTE, LINE_READ_BUCKET_AVG_SIZE_BYTE);
	}

	public static LineReadBucket newLineReadBucket(String filename, int bufSizeByte, int avgBufSize) {
		return new LineReadBucket(filename, bufSizeByte, avgBufSize);
	}

	public static LineWriteBucket newLineWriteBucket(String filename) {
		return new LineWriteBucket(filename, LINE_WRITE_BUCKET_BUF_SIZE_BYTE, LINE_WRITE_BUCKET_MAX_SIZE_BYTE);
	}

	public static LineWriteBucket newLineWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		return new LineWriteBucket(filename, bufSizeByte, maxSizeByte);
	}

	public static LengthReadBucket newLengthReadBucket(String filename) {
		return new LengthReadBucket(filename, LENGTH_READ_BUCKET_BUF_SIZE_BYTE, LENGTH_READ_BUCKET_AVG_SIZE_BYTE);
	}

	public static LengthReadBucket newLengthReadBucket(String filename, int bufSizeByte, int avgSizeByte) {
		return new LengthReadBucket(filename, bufSizeByte, avgSizeByte);
	}

	public static LengthWriteBucket newLengthWriteBucket(String filename) {
		return new LengthWriteBucket(filename, LENGTH_WRITE_BUCKET_BUF_SIZE_BYTE, LENGTH_WRITE_BUCKET_MAX_SIZE_BYTE);
	}

	public static LengthWriteBucket newLengthWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		return new LengthWriteBucket(filename, bufSizeByte, maxSizeByte);
	}
}
