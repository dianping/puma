package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.cleanup.DeleteStrategy;
import com.dianping.puma.storage.cleanup.ExpiredDeleteStrategy;
import com.dianping.puma.storage.index.utils.IndexCodec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class L1SingleWriteIndexManager extends SingleWriteIndexManager<BinlogInfo, Sequence> {

    private DeleteStrategy deleteStrategy = new ExpiredDeleteStrategy();

    public L1SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
        super(file, bufSizeByte, maxSizeByte);
    }

    @Override
    public void append(BinlogInfo indexKey, Sequence indexValue) throws IOException {
        List<Pair<BinlogInfo, Sequence>> result = new ArrayList<Pair<BinlogInfo, Sequence>>();

        if (file != null && file.exists()) {
            flush();
            L1SingleReadIndexManager l1IndexReader = IndexManagerFactory.newL1SingleReadIndexManager(file);
            l1IndexReader.start();

            try {
                while (true) {
                    Pair<BinlogInfo, Sequence> l1index = l1IndexReader.next();

                    if (l1index == null) {
                        break;
                    }

                    if (deleteStrategy.canClean(String.valueOf(l1index.getRight().getCreationDate()))) {
                        continue;
                    }

                    result.add(l1index);
                }
            } finally {
                l1IndexReader.stop();
            }

            FileUtils.forceDelete(file);
        }

        result.add(Pair.of(indexKey, indexValue));

        doStop();
        doStart();
        for (Pair<BinlogInfo, Sequence> pair : result) {
            super.append(pair.getLeft(), pair.getRight());
        }
    }

    @Override
    protected byte[] encode(BinlogInfo binlogInfo, Sequence sequence) {
        return IndexCodec.encodeL1Index(binlogInfo, sequence);
    }
}
