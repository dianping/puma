package com.dianping.puma.storage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

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
	private DataInputStream zipFileInputStream;

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

	@Override
	public byte[] getNext() throws StorageClosedException, IOException {
		checkClosed();
		// we should guarantee the whole packet read in one transaction,
		// otherwise we will skip some bytes and read a wrong value in the next
		// call
		if (this.zipFileInputStream == null) {
			byte[] data = doReadData();
			if (data.toString().equals("ZIPFORMAT")) {
				ByteArrayInputStream bin = new ByteArrayInputStream(
						doReadData());
				this.zipFileInputStream = new DataInputStream(
						new GZIPInputStream(bin));
				return getNextFromZipBuf();
			} else {
				throw new EOFException();
			}
		} else {
			return getNextFromZipBuf();
		}
	}

	public byte[] getNextFromZipBuf() throws IOException {
		try {
			int len = this.zipFileInputStream.readInt();
			byte[] unzipdata = new byte[len];
			this.zipFileInputStream.read(unzipdata);
			return unzipdata;
		} catch (EOFException e) {
			ByteArrayInputStream bin = new ByteArrayInputStream(doReadData());
			this.zipFileInputStream = new DataInputStream(new GZIPInputStream(
					bin));
			try {
				int len = this.zipFileInputStream.readInt();
				byte[] unzipdata = new byte[len];
				this.zipFileInputStream.read(unzipdata);
				return unzipdata;
			} catch (EOFException ee) {
				throw new EOFException();
			}
		}
	}
}