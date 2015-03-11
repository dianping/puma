package com.dianping.puma.core.holder;

import com.dianping.puma.core.model.BinlogInfo;

/**
 * @author Leo Liang
 * 
 */
public interface BinlogInfoHolder {

	public void setBaseDir(String baseDir);

	public BinlogInfo getBinlogInfo(String taskName);

	public void setBinlogInfo(String taskName, BinlogInfo binlogInfo);

	public void remove(String taskName);

	public void clean(String taskName);
}
