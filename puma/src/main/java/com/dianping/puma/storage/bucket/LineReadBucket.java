package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.*;

public final class LineReadBucket extends AbstractLifeCycle implements ReadBucket {

	private final File file;

	private final int bufSizeByte;

	private final int avgSizeByte;

	private BufferedReader reader;

	private int position;

	protected LineReadBucket(File file, int bufSizeByte, int avgSizeByte) {
		this.file = file;
		this.bufSizeByte = bufSizeByte;
		this.avgSizeByte = avgSizeByte;
	}

	@Override
	protected void doStart() {
		try {
			reader = new BufferedReader(new FileReader(file), bufSizeByte);
			if (!reader.markSupported()) {
				throw new RuntimeException("line read bucket should support mark.");
			}
		} catch (FileNotFoundException fnf) {
			throw new RuntimeException("bucket file not found.", fnf);
		}
	}

	@Override
	protected void doStop() {
		try {
			reader.close();
		} catch (IOException ignore) {
		}
	}

	@Override
	public byte[] next() throws IOException {
		checkStop();

		try {
			reader.mark(avgSizeByte);

			String line = reader.readLine();
			if (line == null) {
				throw new EOFException("reach the end of line reader bucket.");
			}

			byte[] data = line.getBytes();
			position += data.length;
			return data;
		} catch (EOFException eof) {
			throw eof;
		} catch (IOException io) {
			try {
				reader.reset();
			} catch (IOException ignore) {
			}

			throw io;
		}
	}

	@Override
	public void skip(long offset) throws IOException {
		checkStop();

		if (offset < 0) {
			throw new IllegalArgumentException("offset is negative.");
		}

		long count = offset;
		while (count > 0) {
			long skipLength = reader.skip(offset);
			count -= skipLength;
		}

		position += offset;
	}

	@Override
	public int position() {
		throw new IllegalAccessError();
	}
}
