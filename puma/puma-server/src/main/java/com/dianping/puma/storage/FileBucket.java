package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.codec.EventCodec;

public class FileBucket implements Bucket {
	private EventCodec codec;

	private RandomAccessFile file;

	public FileBucket(File file) throws FileNotFoundException {
		this.file = new RandomAccessFile(file, "rw");
	}

	@Override
	public void append(ChangedEvent event) throws IOException {
		byte[] data = codec.encode(event);

		file.writeInt(data.length);
		file.write(data);
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
}
