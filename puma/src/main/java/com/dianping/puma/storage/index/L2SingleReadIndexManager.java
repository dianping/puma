package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

public final class L2SingleReadIndexManager extends SingleReadIndexManager<BinlogInfo, Sequence> {

	public static final int ENCODE_NUMBER = 2;
	public static final int BINLOG_INFO_FIELD_NUMBER = 4;
	public static final int SEQUENCE_FIELD_NUMBER = 3;

	public L2SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
		super(file, bufSizeByte, avgSizeByte);
	}

	@Override
	protected boolean greater(BinlogInfo aBinlogInfo, BinlogInfo bBinlogInfo) {
		long aServerId = aBinlogInfo.getServerId();
		long bServerId = bBinlogInfo.getServerId();
		if (aServerId == bServerId) {
			String aBinlogFile = aBinlogInfo.getBinlogFile();
			String bBinlogFile = bBinlogInfo.getBinlogFile();
			int result = aBinlogFile.compareTo(bBinlogFile);
			if (result > 0) {
				return true;
			} else if (result < 0) {
				return false;
			} else {
				long aBinlogPosition = aBinlogInfo.getBinlogPosition();
				long bBinlogPosition = bBinlogInfo.getBinlogPosition();
				return aBinlogPosition - bBinlogPosition > 0;
			}
		} else {
			long aTimestamp = aBinlogInfo.getTimestamp();
			long bTimestamp = bBinlogInfo.getTimestamp();
			return aTimestamp > bTimestamp;
		}
	}

	@Override
	protected Pair<BinlogInfo, Sequence> decode(byte[] data) throws IOException {
		String rawString = new String(data);
		String[] rawStrings = StringUtils.split(rawString, "=");
		if (rawStrings == null || rawStrings.length != ENCODE_NUMBER) {
			throw new IOException("unknown L2 index format.");
		}

		String binlogInfoString = rawStrings[0];
		String[] binlogInfoStrings = StringUtils.split(binlogInfoString, "!");
		if (binlogInfoStrings == null || binlogInfoStrings.length != BINLOG_INFO_FIELD_NUMBER) {
			throw new IOException("unknown L2 index format.");
		}

		long timestamp = Long.valueOf(binlogInfoStrings[0]);
		long serverId = Long.valueOf(binlogInfoStrings[1]);
		String binlogFile = binlogInfoStrings[2];
		long binlogPosition = Long.valueOf(binlogInfoStrings[3]);
		BinlogInfo binlogInfo = new BinlogInfo(timestamp, serverId, binlogFile, binlogPosition);

		String sequenceString = rawStrings[1];
		String[] sequenceStrings = StringUtils.split(sequenceString, "!");
		if (sequenceStrings == null || sequenceStrings.length != SEQUENCE_FIELD_NUMBER) {
			throw new IOException("unknown L2 index format.");
		}

		int date = Integer.valueOf(sequenceStrings[0]);
		int number = Integer.valueOf(sequenceStrings[1]);
		int offset = Integer.valueOf(sequenceStrings[2]);
		Sequence sequence = new Sequence(date, number, offset);

		return Pair.of(binlogInfo, sequence);
	}
}
