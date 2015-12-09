package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface ReadBucket extends LifeCycle {

	/**
	 * Supply support for bucket sequential reading.
	 *
	 * @return next data block.
	 * @throws IOException
	 */
	byte[] next() throws IOException;

	/**
	 * Supply support for bucket random reading.
	 *
	 * @param offset offset to skip.
	 * @throws IOException
	 */
	void skip(long offset) throws IOException;

	/**
	 * Get the offset of the bucket count in byte.
	 *
	 * @return offset count in byte.
	 */
	int position();
}
