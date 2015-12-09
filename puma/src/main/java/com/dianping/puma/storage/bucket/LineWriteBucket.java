package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class LineWriteBucket extends AbstractLifeCycle implements WriteBucket {

	private static final int BYTE_SIZE_BYTE = Byte.SIZE >> 3;

	private final File file;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private BufferedWriter writer;

	private int offset;

	protected LineWriteBucket(File file, int bufSizeByte, int maxSizeByte) {
		this.file = file;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	@Override
	protected void doStart() {
		try {
			writer = new BufferedWriter(new FileWriter(file, true), bufSizeByte);
		} catch (IOException io) {
			throw new RuntimeException("failed to start line write bucket.", io);
		}
	}

	@Override
	protected void doStop() {
		if (writer != null) {
			try {
				writer.flush();
			} catch (IOException ignore) {
			} finally {
				try {
					writer.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	@Override
	public void append(byte[] data) throws IOException {
		checkStop();

		if (data == null) {
			throw new IllegalArgumentException("data");
		}

		String line = new String(data);
		writer.write(line);
		writer.newLine();

		offset += (data.length + BYTE_SIZE_BYTE);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		writer.flush();
	}

	@Override
	public boolean hasRemainingForWrite() {
		return offset < maxSizeByte;
	}

	@Override
	public int position() {
		return offset;
	}
}
