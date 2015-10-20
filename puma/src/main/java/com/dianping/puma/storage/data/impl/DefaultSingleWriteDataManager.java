package com.dianping.puma.storage.data.impl;

public class DefaultSingleWriteDataManager extends SingleWriteDataManager<DataKeyImpl, DataValueImpl> {

	public DefaultSingleWriteDataManager(String filename) {
		super(filename);
	}

	@Override
	protected byte[] encode(DataKeyImpl dataKey, DataValueImpl dataValue) {
		return new byte[0];
	}
}
