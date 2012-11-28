package com.dianping.puma.storage;

import java.io.DataInputStream;
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
		this.compressor.setInputStream(new DataInputStream(inputStream));
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

	protected int readByte() throws StorageClosedException, IOException {
		return inputStream.readInt();
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

	@Override
	protected byte[] doReadDataBlock() throws StorageClosedException, IOException {
		int read = (512 > (this.blocksize - this.nowoff)) ? (this.blocksize - this.nowoff) : 512;
		byte[] data = new byte[read];
		int n = 0;
		while (n < read) {
			checkClosed();
			int count = inputStream.read(data, 0 + n, read - n);
			if(count == -1)
				break;
			n += count;
		}
		byte[] result = new byte[n];
		for(int i=0 ; i<n; i++){
			result[i] = data[i];
		}
		this.nowoff = this.nowoff + n;
		return result;
	}
}