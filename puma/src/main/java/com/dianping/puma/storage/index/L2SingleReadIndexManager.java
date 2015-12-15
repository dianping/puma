package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.index.utils.IndexCodec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

public final class L2SingleReadIndexManager extends SingleReadIndexManager<BinlogInfo, Sequence> {

    public L2SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
        super(file, bufSizeByte, avgSizeByte);
    }

    @Override
    protected boolean greater(BinlogInfo aBinlogInfo, BinlogInfo bBinlogInfo) {
        return aBinlogInfo.greaterThan(bBinlogInfo);
    }

    @Override
    protected Pair<BinlogInfo, Sequence> decode(byte[] data) throws IOException {
       return IndexCodec.decodeL2Index(data);
    }
}
