package com.dianping.puma.storage.data.impl;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class DefaultSingleReadDataManager extends SingleReadDataManager<DataKeyImpl, DataValueImpl> {

	public DefaultSingleReadDataManager(String filename) {
		super(filename);
	}

	@Override
	protected Pair<DataKeyImpl, DataValueImpl> decode(byte[] data) throws IOException {
		return null;
	}
}
