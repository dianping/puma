package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.*;

public final class LocalFileWriteBucket extends AbstractLifeCycle implements WriteBucket {

	private static final int BUF_SIZE_BYTE = 100 * 1024; // 100K.

	private static final int MAX_SIZE_BYTE = 1024 * 1024 * 1024; // 1G.

	private String filename;

	private long offset;

	private DataOutputStream output;

	public LocalFileWriteBucket(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {
		try {
			output = file2Stream(filename);
		} catch (IOException io) {
			throw new RuntimeException("failed to start write bucket.", io);
		}
	}

	@Override
	protected void doStop() {
		try {
			// Flush all buffered contents into disk before closing.
			flush();
			output.close();
		} catch (IOException ignore) {
		}
	}

	@Override
	public void append(byte[] data) throws IOException {
		checkStop();

		output.writeInt(data.length);
		output.write(data);

		offset += (Integer.SIZE + data.length);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		output.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		checkStop();

		return offset < MAX_SIZE_BYTE;
	}

	protected DataOutputStream file2Stream(String filename) throws IOException {
		File file = new File(filename);
		if (!file.canWrite()) {
			throw new IOException("bucket can not write.");
		}

		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), BUF_SIZE_BYTE));
	}
}
