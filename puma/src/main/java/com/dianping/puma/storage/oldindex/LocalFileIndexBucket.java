package com.dianping.puma.storage.oldindex;

import com.dianping.puma.storage.exception.StorageClosedException;

import java.io.*;

public class LocalFileIndexBucket<K, V extends IndexValue<K>> implements IndexBucket<K, V> {

	private final DataInputStream input;

	private final IndexItemConverter<V> valueConvertor;

	private final File file;

	private final String name;

	private long prePosition = 0L;

	private long currentPosition = 0L;

	public LocalFileIndexBucket(String name,File file, IndexItemConverter<V> valueConvertor) throws IOException {
		this.name = name;
		this.file = file;
		this.input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		this.valueConvertor = valueConvertor;
	}

	public LocalFileIndexBucket(String name,File file, IndexItemConverter<V> valueConvertor, K startKey, boolean inclusive) throws IOException {
		this.name = name;
		this.file = file;
		this.input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		this.valueConvertor = valueConvertor;

		if(startKey != null){
			locate(startKey,inclusive);
		}
	}

	@Override
	public void start() throws IOException {
	}

	@Override
	public void stop() throws IOException {
		if (this.input != null) {
			this.input.close();
		}
	}

	@Override
	public V next() throws StorageClosedException, IOException {
		this.input.mark(Integer.MAX_VALUE);
		try {
			int len = this.input.readInt();
			byte[] bytes = new byte[len];
			this.input.readFully(bytes);
			prePosition = currentPosition;
			currentPosition += 4 + len;
			V result = this.valueConvertor.convertFromObj(bytes);
			return result;
		} catch (IOException e) {
			this.input.reset();

			throw e;
		}
	}

	@Override
	public void resetNext() throws IOException {
		this.input.reset();
	}

	private void locate(K key, boolean inclusive) throws StorageClosedException, IOException {
		if (key == null) {
			return;
		}

		while (true) {
			V next = next();

			if (next.getIndexKey().equals(key)) {
				if (inclusive) {
					this.input.reset();
				}

				return;
			}
		}
	}

	@Override
	public void truncate() throws IOException {
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "rwd");
			randomAccessFile.setLength(prePosition);
		} finally {
			if (randomAccessFile != null) {
				randomAccessFile.close();
			}
		}
	}

	// hack for test purpose
	@Override
	public void append(byte[] bytes) throws IOException {
		DataOutputStream output = new DataOutputStream(new FileOutputStream(file, true));

		output.write(bytes);
		output.close();
	}

	@Override
	public String getName() {
		return name;
	}
}
