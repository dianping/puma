package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.utils.ZipUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

public final class LocalFileReadBucket extends AbstractLifeCycle implements ReadBucket {

	private static final int READ_BUF_SIZE = 1024 * 100; // 100k.

	private String filename;

	private DataInputStream input;

	public LocalFileReadBucket(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		try {
			input = file2Stream(filename);

		} catch (IOException io) {
			throw new RuntimeException("failed to start read bucket.");
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
	public byte[] next() throws IOException {
		checkStop();

		try {
			input.mark(Integer.MAX_VALUE);

			int len = input.readInt();
			if (len <= 0) {
				throw new IOException("failed to read next data.");
			}

			byte[] data = new byte[len];
			input.readFully(data);

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
			throw new IOException("failed to skip.");
		}

		long count = offset;
		while (count > 0) {
			long skipLength = input.skip(count);
			count -= skipLength;
		}
	}

	protected DataInputStream file2Stream(String filename) throws IOException {
		File file = new File(filename);
		if (!file.canRead()) {
			throw new IOException("bucket can not read.");
		}

		if (checkCompressed(file)) {
			input = new DataInputStream(new BufferedInputStream(
					new GZIPInputStream(new FileInputStream(file), READ_BUF_SIZE)));
		} else {
			input = new DataInputStream(new BufferedInputStream(new FileInputStream(file), READ_BUF_SIZE));
		}

		return input;
	}

	protected boolean checkCompressed(File file) throws FileNotFoundException {
		return ZipUtils.checkGZip(file);
	}
}
