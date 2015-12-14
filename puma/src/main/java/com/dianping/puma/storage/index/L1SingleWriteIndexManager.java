package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.index.utils.IndexCodec;

import java.io.File;

public final class L1SingleWriteIndexManager extends SingleWriteIndexManager<BinlogInfo, Sequence> {

	public L1SingleWriteIndexManager(File file, int bufSizeByte, int maxSizeByte) {
		super(file, bufSizeByte, maxSizeByte);
	}

	@Override
	protected byte[] encode(BinlogInfo binlogInfo, Sequence sequence) {
		return  IndexCodec.encodeL1Index(binlogInfo,sequence);
	}
}
