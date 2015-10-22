package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

public final class L1SingleWriteIndexManager extends SingleWriteIndexManager<L1IndexKey, L1IndexValue> {

	public L1SingleWriteIndexManager(String filename) {
		super(filename);
	}

	@Override
	protected byte[] encode(L1IndexKey indexKey, L1IndexValue indexValue) {
		BinlogInfo binlogInfo = indexKey.getBinlogInfo();
		Sequence sequence = indexValue.getSequence();

		return new StringBuilder()
				.append(binlogInfo.getTimestamp())
				.append("!")
				.append(binlogInfo.getServerId())
				.append("!")
				.append(binlogInfo.getBinlogFile())
				.append("!")
				.append(binlogInfo.getBinlogPosition())
				.append("=")
				.append(sequence.getCreationDate())
				.append("-")
				.append("Bucket")
				.append("-")
				.append(sequence.getNumber())
				.toString()
				.getBytes();
	}
}
