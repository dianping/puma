package com.dianping.puma.storage.data.impl;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.WriteDataBucket;

import java.io.*;

public class LocalFileWriteDataBucket extends AbstractLifeCycle implements WriteDataBucket {

	private int bufSizeByte = 100 * 1024; // 默认buffer大小为100K

	private int maxSizeByte = 1024 * 1024 * 1024; // 默认文件大小为1G

	private Sequence sequence;

	private File file;

	private DataOutputStream output;

	private int date;

	private int index;

	public LocalFileWriteDataBucket(Sequence sequence, File file) {
		this.sequence = sequence;
		this.file = file;
	}

	public LocalFileWriteDataBucket(Sequence sequence, File file, int maxSizeByte, int bufSizeByte) {
		this.sequence = sequence;
		this.file = file;
		this.maxSizeByte = maxSizeByte;
		this.bufSizeByte = bufSizeByte;
	}

	@Override
	protected void doStart() {
		try {
			output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), bufSizeByte));
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
	public String name() {
		return null;
	}

	@Override
	public Sequence sequence() {
		return sequence;
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
	public boolean hasRemainingForWrite() {
		return sequence.getOffset() < maxSizeByte;
	}
}
