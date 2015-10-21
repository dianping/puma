package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class LineWriteBucket extends AbstractLifeCycle implements WriteBucket {

	private final String filename;

	private final int bufSizeByte;

	private final int maxSizeByte;

	private BufferedWriter writer;

	private long offset;

	protected LineWriteBucket(String filename, int bufSizeByte, int maxSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.maxSizeByte = maxSizeByte;
	}

	@Override
	protected void doStart() {
		try {
			writer = new BufferedWriter(new FileWriter(filename), bufSizeByte);
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
			throw new NullPointerException("data");
		}

		String line = new String(data);
		writer.write(line);
		writer.newLine();

		offset += (data.length + (Byte.SIZE >> 3));
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
}
