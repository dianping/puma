package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

import java.io.File;

public final class L2SingleWriteIndexManager extends SingleWriteIndexManager<L2IndexKey, L2IndexValue> {

	public L2SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
		super(file, bufSizeByte, maxSizeByte);
	}

	@Override
	protected byte[] encode(L2IndexKey indexKey, L2IndexValue indexValue) {
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
				.append("!")
				.append(sequence.getNumber())
				.append("!")
				.append(sequence.getOffset())
				.toString()
				.getBytes();
	}
}
