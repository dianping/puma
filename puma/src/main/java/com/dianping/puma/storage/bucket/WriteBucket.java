package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteBucket extends LifeCycle {

	/**
	 * Append data block to the end of the bucket.
	 *
	 * @param data data block to be appended.
	 * @throws IOException
	 */
	void append(byte[] data) throws IOException;

	/**
	 * Flush the buffered data blocks into disk.
	 *
	 * @throws IOException
	 */
	void flush() throws IOException;

	/**
	 * Check if there is remaining space for writing.
	 *
	 * @return true if has remaining space for writing.
	 */
	boolean hasRemainingForWrite();

	int position();
}
