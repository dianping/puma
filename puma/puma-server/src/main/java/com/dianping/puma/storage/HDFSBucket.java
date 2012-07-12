package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;

public class HDFSBucket implements Bucket {

	private EventCodec			codec;
	private Sequence			startingSequence;
	private volatile boolean	stopped		= false;

	private FSDataInputStream	inputStream	= null;
	private Path				file;

	// private FSDataOutputStream outputStream = null;

	public HDFSBucket(FileSystem fileSystem, String readingPath, Sequence startingSequence, EventCodec codec)
			throws IOException {
		this.startingSequence = startingSequence;
		this.codec = codec;
		this.file = new Path(readingPath);
		this.inputStream = fileSystem.open(file);
	}

	// public void setOutStream(String writingPath) throws IOException {
	// this.outputStream = fileSystem.create(new Path(writingPath));
	// }

	@Override
	public void append(ChangedEvent event) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		stopped = true;
		inputStream.close();
		inputStream = null;
	}

	@Override
	public long getCurrentWritingSeq() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ChangedEvent getNext() throws IOException {
		checkClosed();
		// we should guarantee the whole packet read in one transaction,
		// otherwise we will skip some bytes and read a wrong value in the next
		// call
		if (inputStream.available() > 4) {
			int length = inputStream.readInt();
			byte[] data = new byte[length];
			int n = 0;
			while (n < length) {
				checkClosed();
				int count = inputStream.read(data, 0 + n, length - n);
				n += count;
			}
			return codec.decode(data);
		} else {
			throw new EOFException();
		}
	}

	@Override
	public Sequence getStartingSequece() {
		return startingSequence;
	}

	@Override
	// hdfs does not allowed to write at any position other than the end of the
	// file
	// inputstream seek
	public void seek(int pos) throws IOException {
		checkClosed();
		inputStream.seek(pos);
	}

	private void checkClosed() throws IOException {
		if (stopped) {
			throw new IOException("Bucket has been closed");
		}
	}

	@Override
	public boolean hasRemainingForWrite() throws IOException {
		throw new UnsupportedOperationException();
	}

}