package com.dianping.puma.storage.index.biz;

import com.dianping.puma.storage.Sequence;

public class L1IndexValue {

	private Sequence sequence;

	public L1IndexValue(Sequence sequence) {
		this.sequence = sequence;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}
}
