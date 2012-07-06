package com.dianping.puma.storage;

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
	private long				currentSeq;

	private RandomAccessFile	file;

	public FileBucket(File file, Sequence sequence, int maxSizeMB, EventCodec codec) throws FileNotFoundException {
		this.file = new RandomAccessFile(file, "rw");
		this.startingSequence = sequence;
		this.maxSizeMB = maxSizeMB;
		this.codec = codec;
		currentSeq = startingSequence.longValue();
	}

	@Override
	public void append(ChangedEvent event) throws IOException {
		byte[] data = codec.encode(event);
		file.writeInt(data.length);
		file.write(data);
		currentSeq += 4 + data.length;
	}

	@Override
	public ChangedEvent getNext() throws IOException {
		int length = file.readInt();
		byte[] data = new byte[length];
		file.readFully(data);
		return codec.decode(data);

	}

	@Override
	public void seek(int pos) throws IOException {
		file.seek(pos);
	}

	@Override
	public void close() throws IOException {
		file.close();
	}

	@Override
	public Sequence getStartingSequece() {
		return startingSequence;
	}

	@Override
	public boolean hasRemaining() throws IOException {
		return file.length() < maxSizeMB * 1024 * 1024;
	}

	@Override
	public long getCurrentSeq() {
		return currentSeq;
	}
}
