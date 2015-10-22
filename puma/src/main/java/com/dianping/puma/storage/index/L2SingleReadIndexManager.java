package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public final class L2SingleReadIndexManager extends SingleReadIndexManager<L2IndexKey, L2IndexValue> {

	public L2SingleReadIndexManager(String filename, int bufSizeByte, int avgSizeByte) {
		super(filename, bufSizeByte, avgSizeByte);
	}

	@Override
	protected Pair<L2IndexKey, L2IndexValue> decode(byte[] data) throws IOException {
		String rawString = new String(data);
		String[] rawStrings = StringUtils.split(rawString, "=");
		if (rawStrings == null || rawStrings.length != 2) {
			throw new IOException("unknown L2 index format.");
		}

		String binlogInfoString = rawStrings[0];
		String[] binlogInfoStrings = StringUtils.split(binlogInfoString, "!");
		if (binlogInfoStrings == null || binlogInfoStrings.length != 4) {
			throw new IOException("unknown L2 index format.");
		}

		long timestamp = Long.valueOf(binlogInfoStrings[0]);
		long serverId = Long.valueOf(binlogInfoStrings[1]);
		String binlogFile = binlogInfoStrings[2];
		long binlogPosition = Long.valueOf(binlogInfoStrings[3]);
		BinlogInfo binlogInfo = new BinlogInfo(timestamp, serverId, binlogFile, binlogPosition);

		String sequenceString = rawStrings[1];
		String[] sequenceStrings = StringUtils.split(sequenceString, "!");
		if (sequenceStrings == null || sequenceStrings.length != 3) {
			throw new IOException("unknown L2 index format.");
		}

		int date = Integer.valueOf(sequenceStrings[0]);
		int number = Integer.valueOf(sequenceStrings[1]);
		int offset = Integer.valueOf(sequenceStrings[2]);
		Sequence sequence = new Sequence(date, number, offset);

		return Pair.of(new L2IndexKey(binlogInfo), new L2IndexValue(sequence));
	}
}
