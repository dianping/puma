package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.index.utils.IndexCodec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

public final class L1SingleReadIndexManager extends SingleReadIndexManager<BinlogInfo, Sequence> {

    public L1SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
        super(file, bufSizeByte, avgSizeByte);
    }

    public Pair<BinlogInfo, Sequence> next() throws IOException {
        try {
            return decode(readBucket.next());
        } catch (EOFException eof) {
            return null;
        }
    }

    @Override
    protected boolean greater(BinlogInfo aBinlogInfo, BinlogInfo bBinlogInfo) {
        return aBinlogInfo.greaterThan(bBinlogInfo);
    }

    @Override
    protected Pair<BinlogInfo, Sequence> decode(byte[] data) throws IOException {
        return IndexCodec.decodeL1Index(data);
    }
}
