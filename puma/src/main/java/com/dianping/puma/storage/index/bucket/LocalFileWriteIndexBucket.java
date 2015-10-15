package com.dianping.puma.storage.index.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class LocalFileWriteIndexBucket extends AbstractLifeCycle implements WriteIndexBucket {

	private String filename;

	public LocalFileWriteIndexBucket(String filename) {
		this.filename = filename;
	}

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public void append(byte[] data) throws IOException {

	}

	@Override
	public void flush() throws IOException {

	}
}
