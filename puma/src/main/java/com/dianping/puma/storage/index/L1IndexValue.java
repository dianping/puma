package com.dianping.puma.storage.index;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.index.IndexValue;

public class L1IndexValue implements IndexValue {

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
