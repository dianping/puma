package com.dianping.puma.storage.index.codec;

import com.dianping.puma.common.LifeCycle;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public interface IndexCodec<K, V> extends LifeCycle {

	byte[] encode(K indexKey, V indexValue) throws IOException;

	Pair<K, V> decode(byte[] data) throws IOException;
}
