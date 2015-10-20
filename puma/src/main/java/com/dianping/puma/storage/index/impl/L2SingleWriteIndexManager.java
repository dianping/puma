package com.dianping.puma.storage.index.impl;

public class L2SingleWriteIndexManager extends SingleWriteIndexManager<L2IndexKey, L2IndexValue> {

	public L2SingleWriteIndexManager(String filename) {
		super(filename);
	}

	@Override
	protected byte[] encode(L2IndexKey indexKey, L2IndexValue indexValue) {
		return new byte[0];
	}
}
