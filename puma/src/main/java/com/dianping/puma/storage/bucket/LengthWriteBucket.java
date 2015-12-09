package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.*;

public final class LengthWriteBucket extends AbstractLifeCycle implements WriteBucket {

	private static final int INTEGER_SIZE_BYTE = Integer.SIZE >> 3;

	private final File file;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private int offset;

	private DataOutputStream output;

	protected LengthWriteBucket(File file, int bufSizeByte, int maxSizeByte) {
		this.file = file;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	@Override
	protected void doStart() {
		try {
			output = file2Stream(file);
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

		offset += (INTEGER_SIZE_BYTE + data.length);
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

	@Override
	public int position() {
		return offset;
	}

	protected DataOutputStream file2Stream(File file) throws IOException {
		if (!file.canWrite()) {
			throw new IOException("bucket can not write.");
		}

		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), bufSizeByte));
	}
}
