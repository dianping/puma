package com.dianping.puma.storage.data.bucket;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.bucket.ReadDataBucket;
import com.dianping.puma.utils.ZipUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class LocalFileReadDataBucket extends AbstractLifeCycle implements ReadDataBucket {

	private static final int READ_BUF_SIZE = 1024 * 100; // 100k.

	private String bucketName;

	private Sequence sequence;

	protected File file;

	private DataInputStream input;

	public LocalFileReadDataBucket(Sequence sequence, File file) {
		this.bucketName = file.getName();
		this.sequence = sequence;
		this.file = file;
	}

	@Override
	protected void doStart() {
		try {
			if (checkCompressed()) {
				input = new DataInputStream(new GZIPInputStream(
						new BufferedInputStream(new FileInputStream(file), READ_BUF_SIZE)));
			} else {
				input = new DataInputStream(new BufferedInputStream(new FileInputStream(file), READ_BUF_SIZE));
			}

		} catch (Throwable t) {
			throw new RuntimeException(
					String.format("failed to start local file read data bucket `%s`.", bucketName), t);
		}
	}

	@Override
	protected void doStop() {
		try {
			input.close();
		} catch (IOException ignore) {
		}
	}

	@Override
	public Sequence sequence() {
		return sequence;
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

			sequence.addOffset(readable);
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

		sequence.addOffset(offset);
	}

	protected boolean checkCompressed() throws FileNotFoundException {
		return ZipUtils.checkGZip(file);
	}
}
