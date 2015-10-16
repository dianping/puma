package com.dianping.puma.storage.data;

public interface DataKey {

	long offset();

	void addOffset(long delta);
}
