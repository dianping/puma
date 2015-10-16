package com.dianping.puma.storage.codec;

import com.dianping.puma.common.AbstractLifeCycle;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class DataCodec<K, V> extends AbstractLifeCycle implements Codec<K, V> {

	@Override protected void doStart() {

	}

	@Override protected void doStop() {

	}

	@Override public byte[] encode(K key, V value) throws IOException {
		return new byte[0];
	}

	@Override public Pair<K, V> decode(byte[] data) throws IOException {
		return null;
	}
}
