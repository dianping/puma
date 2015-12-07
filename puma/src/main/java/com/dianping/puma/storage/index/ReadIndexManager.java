package com.dianping.puma.storage.index;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public interface ReadIndexManager<K, V> extends LifeCycle {

    V findOldest() throws IOException;

    V findLatest() throws IOException;

    V find(K indexKey) throws IOException;
}
