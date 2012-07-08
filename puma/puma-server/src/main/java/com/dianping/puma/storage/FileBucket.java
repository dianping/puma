package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;

public class FileBucket implements Bucket {
	private EventCodec			codec;
	private Sequence			startingSequence;
	private int					maxSizeMB;
	private Sequence			currentWritingSeq;
	private volatile boolean	stopped	= false;

	private RandomAccessFile	file;

	public FileBucket(File file, Sequence sequence, int maxSizeMB, EventCodec codec) throws FileNotFoundException {
		this.file = new RandomAccessFile(file, "rw");
		this.startingSequence = sequence;
		this.maxSizeMB = maxSizeMB;
		this.codec = codec;
		// we need to copy the whole instance
		this.currentWritingSeq = new Sequence(sequence.getCreationDate(), sequence.getNumber());
	}

	@Override
	public void append(ChangedEvent event) throws IOException {
		checkClosed();
		byte[] data = codec.encode(event);
		file.writeInt(data.length);
		file.write(data);
		currentWritingSeq.addOffset(4 + data.length);
	}

	@Override
	public ChangedEvent getNext() throws IOException {
		checkClosed();
		// we should guarantee the whole packet read in one transaction,
		// otherwise we will skip some bytes and read a wrong value in the next
		// call
		if (file.getFilePointer() + 4 < file.length()) {
			int length = file.readInt();
			byte[] data = new byte[length];
			int n = 0;
			while (n < length) {
				checkClosed();
				int count = file.read(data, 0 + n, length - n);
				n += count;
			}
			return codec.decode(data);
		} else {
			throw new EOFException();
		}

	}

	private void checkClosed() throws IOException {
		if (stopped) {
			throw new IOException("Bucket has been closed");
		}
	}

	@Override
	public void seek(int pos) throws IOException {
		checkClosed();
		file.seek(pos);
	}

	@Override
	public void close() throws IOException {
		stopped = true;
		file.close();
		file = null;
	}

	@Override
	public Sequence getStartingSequece() {
		return startingSequence;
	}

	@Override
	public boolean hasRemainingForWrite() throws IOException {
		checkClosed();
		return file.length() < maxSizeMB * 1024 * 1024;
	}

	@Override
	public long getCurrentWritingSeq() {
		return currentWritingSeq.longValue();
	}

}
