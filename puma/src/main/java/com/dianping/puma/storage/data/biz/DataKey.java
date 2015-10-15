package com.dianping.puma.storage.data.biz;

import com.dianping.puma.storage.Sequence;

public class DataKey {

	private Sequence sequence;

	public DataKey(Sequence sequence) {
		this.sequence = sequence;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}
}
