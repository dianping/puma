package com.dianping.puma.storage.index;

import org.apache.commons.lang3.tuple.Pair;

public final class L2SingleReadIndexManager extends SingleReadIndexManager<L2IndexKey, L2IndexValue> {

	public L2SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		super(filename, bufSizeByte, avgSizeByte);
	}

	@Override
	protected Pair<L2IndexKey, L2IndexValue> decode(byte[] data) {
		return null;
	}
}
