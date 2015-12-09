package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface ReadDataManager<K, V> extends LifeCycle {

    K position();

    void open(K dataKey) throws IOException;

    V next() throws IOException;

    String getStorageMode();
}
