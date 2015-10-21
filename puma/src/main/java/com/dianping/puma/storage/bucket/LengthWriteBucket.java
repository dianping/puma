package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.*;

public final class LengthWriteBucket extends AbstractLifeCycle implements WriteBucket {

	private final String filename;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private long offset;

	private DataOutputStream output;

	protected LengthWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
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
			output.flush();
		} catch (IOException ignore) {
		} finally {
			try {
				output.close();
			} catch (IOException ignore) {
			}
		}
	}

	@Override
	public void append(byte[] data) throws IOException {
		checkStop();

		output.writeInt(data.length);
		output.write(data);

		offset += ((Integer.SIZE >> 3) + data.length);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		output.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		checkStop();

		return offset < maxSizeByte;
	}

	protected DataOutputStream file2Stream(String filename) throws IOException {
		File file = new File(filename);
		if (!file.canWrite()) {
			throw new IOException("bucket can not write.");
		}

		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), bufSizeByte));
	}
}
