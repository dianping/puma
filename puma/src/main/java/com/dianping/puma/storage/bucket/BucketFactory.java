package com.dianping.puma.storage.bucket;

import java.io.File;

public final class BucketFactory {

	private static final int LINE_READ_BUCKET_BUF_SIZE_BYTE = 1024; // 1K.

	private static final int LINE_READ_BUCKET_AVG_SIZE_BYTE = 1024; // 1K.

	private static final int LINE_WRITE_BUCKET_BUF_SIZE_BYTE = 1024; // 1K.

	private static final int LINE_WRITE_BUCKET_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

	private static final int LENGTH_READ_BUCKET_BUF_SIZE_BYTE = 2 * 1024 * 1024; // 16M.

	private static final int LENGTH_READ_BUCKET_AVG_SIZE_BYTE = 10 * 1024; // 10K.

	private static final int LENGTH_WRITE_BUCKET_BUF_SIZE_BYTE = 2 * 1024 * 1024; // 16M.

	private static final int LENGTH_WRITE_BUCKET_MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

	private BucketFactory() {
	}

	public static LineReadBucket newLineReadBucket(File file) {
		return new LineReadBucket(file, LINE_READ_BUCKET_BUF_SIZE_BYTE, LINE_READ_BUCKET_AVG_SIZE_BYTE);
	}

	public static LineReadBucket newLineReadBucket(File file, int bufSizeByte, int avgBufSize) {
		return new LineReadBucket(file, bufSizeByte, avgBufSize);
	}

	public static LineWriteBucket newLineWriteBucket(File file) {
		return new LineWriteBucket(file, LINE_WRITE_BUCKET_BUF_SIZE_BYTE, LINE_WRITE_BUCKET_MAX_SIZE_BYTE);
	}

	public static LineWriteBucket newLineWriteBucket(File file, int bufSizeByte, int maxSizeByte) {
		return new LineWriteBucket(file, bufSizeByte, maxSizeByte);
	}

	public static LengthReadBucket newLengthReadBucket(File file) {
		return new LengthReadBucket(file, LENGTH_READ_BUCKET_BUF_SIZE_BYTE, LENGTH_READ_BUCKET_AVG_SIZE_BYTE);
	}

	public static LengthReadBucket newLengthReadBucket(File file, int bufSizeByte, int avgSizeByte) {
		return new LengthReadBucket(file, bufSizeByte, avgSizeByte);
	}

	public static LengthWriteBucket newLengthWriteBucket(File file) {
		return new LengthWriteBucket(file, LENGTH_WRITE_BUCKET_BUF_SIZE_BYTE, LENGTH_WRITE_BUCKET_MAX_SIZE_BYTE);
	}

	public static LengthWriteBucket newLengthWriteBucket(File file, int bufSizeByte, int maxSizeByte) {
		return new LengthWriteBucket(file, bufSizeByte, maxSizeByte);
	}
}
