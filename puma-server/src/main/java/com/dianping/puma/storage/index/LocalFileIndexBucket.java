package com.dianping.puma.storage.index;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.dianping.puma.storage.exception.StorageClosedException;

public class LocalFileIndexBucket<K, V> implements IndexBucket<K, V> {

	private DataInputStream input;

	private IndexItemConvertor<V> valueConvertor;

	public LocalFileIndexBucket(File file, IndexItemConvertor<V> valueConvertor) throws IOException {
		this.input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		this.valueConvertor = valueConvertor;
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
		int len = this.input.readByte();
		byte[] bytes = new byte[len];
		this.input.read(bytes);

		return this.valueConvertor.convertFromObj(bytes);
	}

	@Override
	public void locate(K key) throws StorageClosedException, IOException {
		while (true) {
			V next = next();

			if (next instanceof L2Index) {
				L2Index l2Index = (L2Index) next;

				if (l2Index.getBinlogIndexKey().equals(key)) {
					return;
				}
			}
		}

	}

//	@Override
//	public V find(K key) throws StorageClosedException, IOException {
//		while (true) {
//			V next = next();
//
//			if (next instanceof L2Index) {
//				L2Index l2Index = (L2Index) next;
//
//				if (l2Index.getBinlogIndexKey().equals(key)) {
//					return next;
//				}
//			}
//		}
//	}
}
