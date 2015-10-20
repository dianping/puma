package com.dianping.puma.storage.index.impl;

public class L1SingleWriteIndexManager extends SingleWriteIndexManager<L1IndexKey, L1IndexValue> {

	public L1SingleWriteIndexManager(String filename) {
		super(filename);
	}

	@Override
	protected byte[] encode(L1IndexKey indexKey, L1IndexValue indexValue) {
		return new byte[0];
	}
}
