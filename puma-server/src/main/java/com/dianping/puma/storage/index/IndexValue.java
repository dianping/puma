package com.dianping.puma.storage.index;

public interface IndexValue<K> {

	K getIndexKey();

	boolean isTransactionBegin();

	boolean isTransactionCommit();

}
