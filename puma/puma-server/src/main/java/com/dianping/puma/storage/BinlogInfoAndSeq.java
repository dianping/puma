package com.dianping.puma.storage;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.dianping.puma.core.event.ChangedEvent;

public class BinlogInfoAndSeq implements Comparable<BinlogInfoAndSeq> {

    private long                serverId;
    private String              binlogFile;
    private long                binlogPosition;
    private long                seq;

    private static final String BINLOGINFO_SEPARATOR         = "$";
    private static final String BINLOGINFO_SEPARATOR_PATTERN = "\\$";

    public BinlogInfoAndSeq() {
        super();
    }

    public BinlogInfoAndSeq(long serverId, String binlogFile, long binlogPosition, long seq) {
        super();
        this.serverId = serverId;
        this.binlogFile = binlogFile;
        this.binlogPosition = binlogPosition;
        this.seq = seq;
    }

    public static BinlogInfoAndSeq getBinlogInfoAndSeq(ChangedEvent event) {
        return new BinlogInfoAndSeq(event.getServerId(), event.getBinlog(), event.getBinlogPos(), event.getSeq());
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getBinlogFile() {
        return this.binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    public long getBinlogPosition() {
        return this.binlogPosition;
    }

    public void setBinlogPosition(long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public String toString() {
        return String.valueOf(this.serverId) + BINLOGINFO_SEPARATOR + this.binlogFile + BINLOGINFO_SEPARATOR
                + String.valueOf(this.binlogPosition) + BINLOGINFO_SEPARATOR + String.valueOf(this.seq)
                + BINLOGINFO_SEPARATOR;
    }

    public static BinlogInfoAndSeq valueOf(String key) {
        BinlogInfoAndSeq res = new BinlogInfoAndSeq();

        String[] splits = key.split(BINLOGINFO_SEPARATOR_PATTERN);

        if (splits != null && splits.length == 4) {
            res.setServerId(Long.valueOf(splits[0]));
            res.setBinlogFile(splits[1]);
            res.setBinlogPosition(Long.valueOf(splits[2]));
            res.setSeq(Long.valueOf(splits[3]));
            return res;
        } else {
            return null;
        }

    }

    public void setBinlogInfo(ChangedEvent event) {
        this.serverId = event.getServerId();
        this.binlogFile = event.getBinlog();
        this.binlogPosition = event.getBinlogPos();
    }

    public boolean binlogInfoEqual(BinlogInfoAndSeq value) {
        if (this.serverId == value.getServerId() && this.binlogFile.equals(value)
                && this.binlogPosition == value.getBinlogPosition()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(BinlogInfoAndSeq o) {
        return CompareToBuilder.reflectionCompare(this, o, new String[] { "seq" });
    }

}
