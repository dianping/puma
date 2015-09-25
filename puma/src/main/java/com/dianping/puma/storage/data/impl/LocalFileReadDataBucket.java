package com.dianping.puma.storage.data.impl;

import com.dianping.puma.storage.data.AbstractReadDataBucket;
import com.dianping.puma.utils.ZipUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class LocalFileReadDataBucket extends AbstractReadDataBucket {

	private static final int READ_BUF_SIZE = 1024 * 100; // 100k.

	protected File file;

	public LocalFileReadDataBucket(File file) {
		super(file.getName());
		this.file = file;
	}

	@Override
	protected void doStart() {
		try {
			if (checkCompressed()) {
				input = new DataInputStream(new GZIPInputStream(
								new BufferedInputStream(new FileInputStream(file), READ_BUF_SIZE)));
			} else {
				input = new DataInputStream(new BufferedInputStream(new FileInputStream(file), READ_BUF_SIZE));
			}

		} catch (Throwable t) {
			throw new RuntimeException(
					String.format("failed to start local file read data bucket `%s`.", bucketName), t);
		}
	}

	@Override
	protected void doStop() {
		try {
			input.close();
		} catch (IOException ignore) {
		}
	}

	@Override
	protected boolean checkCompressed() throws FileNotFoundException {
		return ZipUtils.checkGZip(file);
	}
}
