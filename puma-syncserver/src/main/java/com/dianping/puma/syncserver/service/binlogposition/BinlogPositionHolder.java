package com.dianping.puma.syncserver.service.binlogposition;

import com.dianping.puma.bo.PositionInfo;

public interface BinlogPositionHolder {
	public void setBaseDir(String baseDir);

	public PositionInfo getPositionInfo(String syncserverName, String defaultBinlogFile, Long defaultBinlogPos);

	public void savePositionInfo(String syncserverName, PositionInfo positionInfo);
}
