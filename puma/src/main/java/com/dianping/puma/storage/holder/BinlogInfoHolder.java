package com.dianping.puma.storage.holder;

import com.dianping.puma.core.model.BinlogInfo;

/**
 * @author Leo Liang
 */
public interface BinlogInfoHolder {

    BinlogInfo getBinlogInfo(String taskName);

    void setBinlogInfo(String taskName, BinlogInfo binlogInfo);

    void rename(String oriTaskName, String taskName);

    void remove(String taskName);
}