package com.dianping.puma.storage.index;

import com.dianping.puma.storage.exception.StorageClosedException;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

public class LocalFileIndexBucket<K, V extends IndexValue<K>> implements IndexBucket<K, V> {

	private DataInputStream input;

	private IndexItemConvertor<V> valueConvertor;

	private File file;

	private long prePosition = 0L;

	private long currentPosition = 0L;

	private final AtomicReference<K> startIndexKey = new AtomicReference<K>();

	public LocalFileIndexBucket(File file, IndexItemConvertor<V> valueConvertor) throws IOException {
		this.file = file;
		this.input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		this.valueConvertor = valueConvertor;
	}

	public LocalFileIndexBucket(File file, IndexItemConvertor<V> valueConvertor, K startKey, boolean inclusive) throws IOException {
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
			int readable = this.input.read(bytes);

			if (readable != len) {
				throw new IOException("found broken index!");
			}

			prePosition = currentPosition;
			currentPosition += 4 + len;
			V result = this.valueConvertor.convertFromObj(bytes);
			startIndexKey.compareAndSet(null, result.getIndexKey());
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

	@Override
	public K getStartKeyIndex() {
		return startIndexKey.get();
	}

	// hack for test purpose
	@Override
	public void append(byte[] bytes) throws IOException {
		DataOutputStream output = new DataOutputStream(new FileOutputStream(file, true));

		output.write(bytes);
		output.close();
	}
}
