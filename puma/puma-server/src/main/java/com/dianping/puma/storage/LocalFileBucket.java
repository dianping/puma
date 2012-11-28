package com.dianping.puma.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 基于本地文件的Bucket实现
 * 
 * @author Leo Liang
 * 
 */
public class LocalFileBucket extends AbstractBucket {

	private RandomAccessFile file;

	public LocalFileBucket(File file, Sequence startingSequence, int maxSizeMB) throws FileNotFoundException {
		super(startingSequence, maxSizeMB);
		this.file = new RandomAccessFile(file, "rw");
		FileInputStream inputStream = new FileInputStream(file);
		//TODO refactor better
		try { 
			if (inputStream.available() >= 24) {
				inputStream.skip(24);
			}
		} catch (Exception e) {
			// ignore
		}
		this.compressor.setInputStream(new DataInputStream(inputStream));
	}

	protected void doAppend(byte[] data) throws IOException {
		file.write(data);
	}

	protected int readByte() throws StorageClosedException, IOException {
		return file.readInt();
	}

	protected byte[] doReadData() throws StorageClosedException, IOException {
		int length = file.readInt();
		byte[] data = new byte[length];
		int n = 0;
		while (n < length) {
			checkClosed();
			int count = file.read(data, 0 + n, length - n);
			n += count;
		}
		return data;
	}

	protected byte[] doReadDataBlock() throws StorageClosedException, IOException {
		int read = (512 > (this.blocksize - this.nowoff)) ? (this.blocksize - this.nowoff) : 512;
		byte[] data = new byte[read];
		int n = 0;
		while (n < read) {
			checkClosed();
			int count = file.read(data, 0 + n, read - n);
			if (count == -1)
				break;
			n += count;
		}
		byte[] result = new byte[n];
		for (int i = 0; i < n; i++) {
			result[i] = data[i];
		}
		this.nowoff = this.nowoff + n;
		return result;
	}

	protected boolean readable() throws IOException {
		return file.getFilePointer() + 4 < file.length();
	}

	protected void doSeek(int pos) throws IOException {
		file.seek(pos);
	}

	protected void doClose() throws IOException {
		file.close();
		file = null;
	}

	protected boolean doHasRemainingForWrite() throws IOException {
		return file.length() < getMaxSizeByte();
	}
}
