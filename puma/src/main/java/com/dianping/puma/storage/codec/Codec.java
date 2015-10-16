package com.dianping.puma.storage.codec;

import com.dianping.puma.common.LifeCycle;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public interface Codec<K, V> extends LifeCycle {

	byte[] encode(K key, V value) throws IOException;

	Pair<K, V> decode(byte[] data) throws IOException;
}
