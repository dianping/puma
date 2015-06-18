package com.dianping.puma.api.manager;

import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.model.BinlogInfo;

public interface PositionManager extends LifeCycle<PumaException> {

	void open();

	void close();

	BinlogInfo next();

	void save(BinlogInfo binlogInfo);
}
