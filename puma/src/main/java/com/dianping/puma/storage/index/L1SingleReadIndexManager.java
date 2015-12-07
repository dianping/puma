package com.dianping.puma.storage.index;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

public final class L1SingleReadIndexManager extends SingleReadIndexManager<BinlogInfo, Sequence> {

    public static final int BINLOG_INFO_FIELD_NUMBER = 4;
    public static final int SEQUENCE_FIELD_NUMBER = 3;
    public static final int DECODE_NUMBER = 2;

    public L1SingleReadIndexManager(File file, int bufSizeByte, int avgSizeByte) {
        super(file, bufSizeByte, avgSizeByte);
    }

    public Pair<BinlogInfo, Sequence> next() throws IOException {
        try {
            return decode(readBucket.next());
        } catch (EOFException eof) {
            return null;
        }
    }

    @Override
    protected boolean greater(BinlogInfo aBinlogInfo, BinlogInfo bBinlogInfo) {
        long aServerId = aBinlogInfo.getServerId();
        long bServerId = bBinlogInfo.getServerId();
        if (aServerId != 0 && bServerId != 0 && aServerId == bServerId) {
            String aBinlogFile = aBinlogInfo.getBinlogFile();
            String bBinlogFile = bBinlogInfo.getBinlogFile();

            Integer aBinlogFileNumber = Integer.valueOf(StringUtils.substringAfterLast(aBinlogFile, "."));
            Integer bBinlogFileNumber = Integer.valueOf(StringUtils.substringAfterLast(bBinlogFile, "."));
            int result = aBinlogFileNumber.compareTo(bBinlogFileNumber);

            if (result > 0) {
                return true;
            } else if (result < 0) {
                return false;
            } else {
                long aBinlogPosition = aBinlogInfo.getBinlogPosition();
                long bBinlogPosition = bBinlogInfo.getBinlogPosition();
                return aBinlogPosition > bBinlogPosition;
            }
        } else {
            long aTimestamp = aBinlogInfo.getTimestamp();
            long bTimestamp = bBinlogInfo.getTimestamp();
            if (aTimestamp == 0 || bTimestamp == 0) {
                return true;
            }
            return aTimestamp > bTimestamp;
        }
    }

    @Override
    protected Pair<BinlogInfo, Sequence> decode(byte[] data) throws IOException {
        String rawString = new String(data);
        String[] rawStrings = StringUtils.split(rawString, "=");
        if (rawStrings == null || rawStrings.length != DECODE_NUMBER) {
            throw new IOException("unknown L1 index format.");
        }

        String binlogInfoString = rawStrings[0];
        String[] binlogInfoStrings = StringUtils.split(binlogInfoString, "!");
        if (binlogInfoStrings == null || binlogInfoStrings.length != BINLOG_INFO_FIELD_NUMBER) {
            throw new IOException("unknown L1 index format for binlog info.");
        }

        long timestamp = Long.valueOf(binlogInfoStrings[0]);
        long serverId = Long.valueOf(binlogInfoStrings[1]);
        String binlogFile = binlogInfoStrings[2];
        long binlogPosition = Long.valueOf(binlogInfoStrings[3]);
        BinlogInfo binlogInfo = new BinlogInfo(serverId, binlogFile, binlogPosition, 0, timestamp);

        String sequenceString = rawStrings[1];
        String[] sequenceStrings = StringUtils.split(sequenceString, "-");
        if (sequenceStrings == null || sequenceStrings.length != SEQUENCE_FIELD_NUMBER) {
            throw new IOException("unknown L1 index format for sequence.");
        }

        int date = Integer.valueOf(sequenceStrings[0]);
        int index = Integer.valueOf(sequenceStrings[2]);
        Sequence sequence = new Sequence(date, index, 0);

        return Pair.of(binlogInfo, sequence);
    }
}
