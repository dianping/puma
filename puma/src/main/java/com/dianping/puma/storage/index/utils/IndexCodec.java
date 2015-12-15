package com.dianping.puma.storage.index.utils;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class IndexCodec {
    public static final int BINLOG_INFO_FIELD_NUMBER = 4;
    public static final int SEQUENCE_FIELD_NUMBER = 3;
    public static final int L1_DECODE_NUMBER = 2;
    public static final int L2_ENCODE_NUMBER = 2;

    private IndexCodec() {
    }

    public static byte[] encodeL1Index(BinlogInfo binlogInfo, Sequence sequence) {
        return (String.valueOf(binlogInfo.getTimestamp()) +
                "!" +
                binlogInfo.getServerId() +
                "!" +
                binlogInfo.getBinlogFile() +
                "!" +
                binlogInfo.getBinlogPosition() +
                "=" +
                sequence.getCreationDate() +
                "-" +
                "Bucket" +
                "-" +
                sequence.getNumber())
                .getBytes();
    }


    public static byte[] encodeL2Index(BinlogInfo binlogInfo, Sequence sequence) {
        return (String.valueOf(binlogInfo.getTimestamp()) +
                "!" +
                binlogInfo.getServerId() +
                "!" +
                binlogInfo.getBinlogFile() +
                "!" +
                binlogInfo.getBinlogPosition() +
                "=" +
                sequence.getCreationDate() +
                "!" +
                sequence.getNumber() +
                "!" +
                sequence.getOffset())
                .getBytes();
    }

    public static Pair<BinlogInfo, Sequence> decodeL1Index(byte[] data) throws IOException {
        String rawString = new String(data);
        String[] rawStrings = StringUtils.split(rawString, "=");
        if (rawStrings == null || rawStrings.length != L1_DECODE_NUMBER) {
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

    public static Pair<BinlogInfo, Sequence> decodeL2Index(byte[] data) throws IOException {
        String rawString = new String(data);
        String[] rawStrings = StringUtils.split(rawString, "=");
        if (rawStrings == null || rawStrings.length != L2_ENCODE_NUMBER) {
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
