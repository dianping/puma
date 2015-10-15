package com.dianping.puma.storage.index.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.IOException;

public class LocalFileReadIndexBucket extends AbstractLifeCycle implements ReadIndexBucket {

	private String filename;

	public LocalFileReadIndexBucket(String filename) {
		this.filename = filename;
	}

	@Override protected void doStart() {

	}

	@Override protected void doStop() {

	}

	@Override public byte[] next() throws IOException {
		return new byte[0];
	}

	@Override public void skip(long offset) throws IOException {

	}
}
