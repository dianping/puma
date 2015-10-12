package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.WriteDataBucket;

import java.io.*;

public class LocalFileWriteDataBucket extends AbstractLifeCycle implements WriteDataBucket {

	/**
	 * Due to the design of {@link Sequence}, the max size mb is 4096.
	 */
	private final long maxSizeMB = 1024;

	private Sequence sequence;

	private File file;

	private DataOutputStream output;

	public LocalFileWriteDataBucket(Sequence sequence, File file) {
		this.sequence = sequence;
		this.file = file;
	}

	@Override
	protected void doStart() {
		try {
			output = new DataOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("failed to start local file write data bucket.", e);
		}
	}

	@Override
	protected void doStop() {
		try {
			output.close();
		} catch (IOException ignore) {
		}
	}

	@Override
	public void append(byte[] data) throws IOException {
		checkStop();

		output.writeInt(data.length);
		output.write(data);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		output.flush();
	}

	@Override
	public boolean hasRemainingForWrite() throws IOException {
		return sequence.getOffset() < maxSizeMB * 1024L * 1024L;
	}
}
