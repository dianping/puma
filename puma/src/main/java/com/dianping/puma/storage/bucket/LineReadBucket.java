package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.*;

public class LineReadBucket extends AbstractLifeCycle implements ReadBucket {

	private String filename;

	private BufferedReader reader;

	public LineReadBucket(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("bucket file not found.");
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
			reader.mark(Integer.MAX_VALUE);

			String line = reader.readLine();
			if (line == null) {
				throw new EOFException("reach the end of line reader bucket.");
			}

			return line.getBytes();
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
	}
}
