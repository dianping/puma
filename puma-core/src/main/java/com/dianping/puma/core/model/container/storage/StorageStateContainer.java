package com.dianping.puma.core.model.container.storage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.container.AbstractStateContainer;
import com.dianping.puma.core.model.state.storage.StorageState;

import org.springframework.stereotype.Service;

@Service("storageStateContainer")
public class StorageStateContainer extends AbstractStateContainer<StorageState> {

	public void setSeq(String name, long seq) {
		StorageState storageState = get(name);
		if (storageState != null) {
			storageState.setSeq(seq);
		}
	}

	public void setBinlogInfo(String name, BinlogInfo binlogInfo) {
		StorageState storageState = get(name);
		if (storageState != null) {
			storageState.setBinlogInfo(binlogInfo);
		}
	}
}
