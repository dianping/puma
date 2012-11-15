package com.dianping.puma.storage;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 基于HDFS的Bucket实现
 * 
 * @author Leo Liang
 * 
 */
public class HDFSBucket extends AbstractBucket {

	private FSDataInputStream inputStream = null;
	private Path file;

	public HDFSBucket(FileSystem fileSystem, String baseDir, String path,
			Sequence startingSequence) throws IOException {
		super(startingSequence, -1);
		this.file = new Path(baseDir, path);
		this.inputStream = fileSystem.open(file);
	}

	protected void doAppend(byte[] data) throws IOException {
		throw new UnsupportedOperationException();
	}

	protected void doClose() throws IOException {
		inputStream.close();
		inputStream = null;
	}

	@Override
	public long getCurrentWritingSeq() {
		throw new UnsupportedOperationException();
	}

	protected boolean readable() throws IOException {
		return inputStream.available() > 4;
	}

	protected byte[] doReadData() throws StorageClosedException, IOException {
		int length = inputStream.readInt();
		byte[] data = new byte[length];
		int n = 0;
		while (n < length) {
			checkClosed();
			int count = inputStream.read(data, 0 + n, length - n);
			n += count;
		}
		return data;
	}

	protected void doSeek(int pos) throws IOException {
		inputStream.seek(pos);
	}

	protected boolean doHasRemainingForWrite() throws IOException {
		throw new UnsupportedOperationException();
	}
}