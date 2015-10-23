package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface DataManagerFinder extends LifeCycle {

	ReadDataManager<DataKeyImpl, DataValueImpl> findMasterReadDataManager(DataKeyImpl dataKey) throws IOException;

	ReadDataManager<DataKeyImpl, DataValueImpl> findSlaveReadDataManager(DataKeyImpl dataKey) throws IOException;

	ReadDataManager<DataKeyImpl, DataValueImpl> findNextMasterReadDataManager(DataKeyImpl dataKey) throws IOException;

	ReadDataManager<DataKeyImpl, DataValueImpl> findNextSlaveReadDataManager(DataKeyImpl dataKey) throws IOException;

	WriteDataManager<DataKeyImpl, DataValueImpl> findNextMasterWriteDataManager() throws IOException;

	WriteDataManager<DataKeyImpl, DataValueImpl> findNextSlaveWriteDataManager() throws IOException;
}
