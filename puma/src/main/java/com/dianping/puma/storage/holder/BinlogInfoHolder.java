package com.dianping.puma.storage.holder;

import com.dianping.puma.core.model.BinlogInfo;

/**
 * @author Leo Liang
 */
public interface BinlogInfoHolder {

    public void setBaseDir(String baseDir);

    public void setBakDir(String bakDir);

    public void setMasterStorageBaseDir(String masterStorageBaseDir);

    public void setSlaveStorageBaseDir(String slaveStorageBaseDir);

    public void setBinlogIndexBaseDir(String binlogIndexBaseDir);

    public void setStorageBakDir(String storageBakDir);

    public BinlogInfo getBinlogInfo(String taskName);

    public void setBinlogInfo(String taskName, BinlogInfo binlogInfo);

    public void rename(String oriTaskName, String taskName);

    public void remove(String taskName);

    public void clean(String taskName);
}