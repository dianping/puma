package com.dianping.puma.storage.index;

public final class L2SingleWriteIndexManager extends SingleWriteIndexManager<L2IndexKey, L2IndexValue> {

	public L2SingleWriteIndexManager(String filename, int bufSizeByte, int maxSizeByte) {
		super(filename, bufSizeByte, maxSizeByte);
	}

	@Override
	protected byte[] encode(L2IndexKey indexKey, L2IndexValue indexValue) {
		return new byte[0];
	}
}
