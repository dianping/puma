package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.File;
import java.io.IOException;

public interface DataManagerFinder extends LifeCycle {

	public ReadDataManager<DataKeyImpl, DataValueImpl> findMasterReadDataManager(DataKeyImpl dataKey) throws IOException;

	public ReadDataManager<DataKeyImpl, DataValueImpl> findSlaveReadDataManager(DataKeyImpl dataKey) throws IOException;

	public ReadDataManager<DataKeyImpl, DataValueImpl> findNextMasterReadDataManager(DataKeyImpl dataKey) throws IOException;

	public ReadDataManager<DataKeyImpl, DataValueImpl> findNextSlaveReadDataManager(DataKeyImpl dataKey) throws IOException;

	public WriteDataManager<DataKeyImpl, DataValueImpl> findNextMasterWriteDataManager() throws IOException;

	public WriteDataManager<DataKeyImpl, DataValueImpl> findNextSlaveWriteDataManager() throws IOException;
}
