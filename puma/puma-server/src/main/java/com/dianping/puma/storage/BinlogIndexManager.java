package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

public interface BinlogIndexManager extends LifeCycle<IOException> {

    public long tranBinlogIndexToSeq(BinlogInfoAndSeq binlogInfoAndSeq) throws StorageClosedException, IOException;

    public void updateBinlogIndex(Bucket bucket, BinlogInfoAndSeq bpas) throws IOException;

    public void deleteBinlogIndex(String path);
    
}
