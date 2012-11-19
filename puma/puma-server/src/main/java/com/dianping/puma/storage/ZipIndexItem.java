package com.dianping.puma.storage;

public class ZipIndexItem {
	private long benginseq;
	private long endseq;
	private long offset;

	public ZipIndexItem(long benginseq, long endseq, long offset) {
		super();
		this.benginseq = benginseq;
		this.endseq = endseq;
		this.offset = offset;
	}

	public long getBeginseq() {
		return benginseq;
	}

	public void setBeginseq(long benginseq) {
		this.benginseq = benginseq;
	}

	public long getEndseq() {
		return endseq;
	}

	public void setEndseq(long endseq) {
		this.endseq = endseq;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

}
