package com.dianping.puma.storage.index.impl;

import org.apache.commons.lang3.tuple.Pair;

public class L2SingleReadIndexManager extends SingleReadIndexManager<L2IndexKey, L2IndexValue> {

	public L2SingleReadIndexManager(String filename) {
		super(filename);
	}

	@Override
	protected Pair<L2IndexKey, L2IndexValue> decode(byte[] data) {
		return null;
	}
}
