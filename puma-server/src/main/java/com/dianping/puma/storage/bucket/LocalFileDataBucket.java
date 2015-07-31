package com.dianping.puma.storage.bucket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.dianping.puma.storage.Sequence;

/**
 * 基于本地文件的Bucket实现
 * 
 * @author Leo Liang
 * 
 */
public class LocalFileDataBucket extends AbstractDataBucket {

	private static final int BUF_SIZE = 1024 * 8;

	public LocalFileDataBucket(File file, Sequence startingSequence, int maxSizeMB, String fileName, boolean compress)
	      throws IOException {
		super(startingSequence, maxSizeMB, fileName, compress);
		this.length = file.length();
		this.output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
		if (!compress) {
			input = new DataInputStream(new BufferedInputStream(new FileInputStream(file), BUF_SIZE));
		} else {
			input = new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file), BUF_SIZE)));
		}
	}

	protected void doAppend(byte[] data) throws IOException {
		this.length += data.length;
		this.output.write(data);
	}

	protected void doClose() throws IOException {
		this.output.close();
	}

	protected boolean doHasRemainingForWrite() throws IOException {
		return this.length < getMaxSizeByte();
	}
}
