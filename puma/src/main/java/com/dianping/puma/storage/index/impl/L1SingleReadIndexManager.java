package com.dianping.puma.storage.index.impl;

import org.apache.commons.lang3.tuple.Pair;

public class L1SingleReadIndexManager extends SingleReadIndexManager<L1IndexKey, L1IndexValue> {

	public L1SingleReadIndexManager(String filename) {
		super(filename);
	}

	@Override
	protected Pair<L1IndexKey, L1IndexValue> decode(byte[] data) {
		return null;
	}
}
