package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.File;
import java.io.IOException;

public interface DataManagerFinder extends LifeCycle {

	public File rootDir();

	public ReadDataManager<DataKeyImpl, DataValueImpl> findReadDataBucket(DataKeyImpl dataKey) throws IOException;

	public ReadDataManager<DataKeyImpl, DataValueImpl> findNextReadDataBucket(DataKeyImpl dataKey) throws IOException;

	public WriteDataManager<DataKeyImpl, DataValueImpl> genNextWriteDataBucket() throws IOException;
}
