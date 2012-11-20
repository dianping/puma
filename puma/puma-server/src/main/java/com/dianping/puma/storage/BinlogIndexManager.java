package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.storage.exception.StorageClosedException;

public interface BinlogIndexManager extends LifeCycle<IOException> {

    public void setCodec(EventCodec codec);

    public long tranBinlogIndexToSeq(BinlogInfoAndSeq binlogInfoAndSeq) throws StorageClosedException, IOException;

    public void flushBinlogIndex(BinlogInfoAndSeq binlogInfoAndSeq) throws IOException;

    public void openBinlogIndex(Sequence seq) throws IOException;

    public void updateSubBinlogIndex(BinlogInfoAndSeq bpas) throws IOException;

    public void updateMainBinlogIndex(Bucket bucket);

    public void stop();

    public void deleteBinlogIndex(String path);

    public void setSubBinlogIndexBaseDir(String subBinlogIndexBaseDir);

    public void setBinlogIndex(TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> binlogIndex);

    public void setMainBinlogIndexFile(File mainBinlogIndexFile);
}
