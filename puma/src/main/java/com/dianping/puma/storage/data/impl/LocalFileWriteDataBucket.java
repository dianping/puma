package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.WriteDataBucket;

import java.io.*;

public class LocalFileWriteDataBucket extends AbstractLifeCycle implements WriteDataBucket {

	/**
	 * Due to the design of {@link Sequence}, the max size mb is 4096L * 1024L * 1024L.
	 */
	private final long maxSizeByte;

	private Sequence sequence;

	private File file;

	private DataOutputStream output;

	public LocalFileWriteDataBucket(Sequence sequence, File file, long maxSizeByte) {
		this.sequence = sequence;
		this.file = file;
		this.maxSizeByte = maxSizeByte;
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
		sequence.incrOffset(4);

		output.write(data);
		sequence.incrOffset(data.length);
	}

	@Override
	public void flush() throws IOException {
		checkStop();

		output.flush();
	}

	@Override
	public boolean hasRemainingForWrite() throws IOException {
		return sequence.getOffset() < maxSizeByte;
	}
}
