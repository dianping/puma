package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

import java.io.File;

public final class L1SingleWriteIndexManager extends SingleWriteIndexManager<BinlogInfo, Sequence> {

	public L1SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
		super(file, bufSizeByte, maxSizeByte);
	}

	@Override
	protected byte[] encode(BinlogInfo binlogInfo, Sequence sequence) {
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
