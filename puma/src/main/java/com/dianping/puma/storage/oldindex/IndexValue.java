package com.dianping.puma.storage.oldindex;

public interface IndexValue<K> {

	K getIndexKey();

	boolean isTransactionBegin();

	boolean isTransactionCommit();

}
