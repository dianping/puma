package com.dianping.puma.storage.index.codec;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class DefaultIndexCodec<K, V> implements IndexCodec<K, V> {

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public byte[] encode(K indexKey, V indexValue) throws IOException {
		return new byte[0];
	}

	@Override
	public Pair<K, V> decode(byte[] data) throws IOException {
		return null;
	}
}
