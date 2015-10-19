package com.dianping.puma.storage.index;

public interface IndexKey<T> {

	int compareTo(T indexKey);
}
