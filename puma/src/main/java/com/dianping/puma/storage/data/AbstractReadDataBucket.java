package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AbstractReadDataBucket extends AbstractLifeCycle implements ReadDataBucket {

	protected String bucketName;

	protected DataInputStream input;

	private long offset;

	protected AbstractReadDataBucket(String bucketName) {
		this.bucketName = bucketName;
	}

	@Override
	public long offset() {
		return offset;
	}

	@Override
	public byte[] next() throws IOException {
		checkStop();

		try {
			input.mark(Integer.MAX_VALUE);

			int len = input.readInt();
			if (len <= 0) {
				throw new IOException(String.format(
						"broken data found, expected to read %d bytes in bucket `%s`.", len, bucketName));
			}

			byte[] data = new byte[len];
			int readable = input.read(data);
			if (readable != len) {
				throw new IOException(String.format("broken data found, expected to "
						+ "read %d bytes but read %s bytes in bucket `%s`.", readable, len, bucketName));
			}

			offset += readable;
			return data;
		} catch (IOException io) {
			try {
				input.reset();
			} catch (IOException ignore) {
			}

			throw io;
		}
	}

	@Override
	public void skip(long offset) throws IOException {
		checkStop();

		if (offset < 0) {
			throw new IOException(String.format(
					"failed to skip %s bytes in bucket `%s`.", offset, bucketName));
		}

		long count = offset;
		while (count > 0) {
			long skipLength = input.skip(count);
			count -= skipLength;
		}

		this.offset -= offset;
	}

	protected abstract boolean checkCompressed() throws FileNotFoundException;
}
