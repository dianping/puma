package com.dianping.puma.core.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class BinlogInfo implements Serializable, Comparable<BinlogInfo> {

    private static final long serialVersionUID = 5056491879587690001L;

    private long serverId;

    private String binlogFile;

    private long binlogPosition;

    private int eventIndex;

    private long timestamp;

    public BinlogInfo() {
    }

    public BinlogInfo(long serverId, String binlogFile, Long binlogPosition, int eventIndex, long timestamp) {
        this.serverId = serverId;
        this.binlogFile = binlogFile;
        this.binlogPosition = binlogPosition;
        this.eventIndex = eventIndex;
        this.timestamp = timestamp;
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public BinlogInfo setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
        return this;
    }

    public long getBinlogPosition() {
        return binlogPosition;
    }

    public BinlogInfo setBinlogPosition(long binlogPosition) {
        this.binlogPosition = binlogPosition;
        return this;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public BinlogInfo setEventIndex(int eventIndex) {
        this.eventIndex = eventIndex;
        return this;
    }

    public long getServerId() {
        return serverId;
    }

    public BinlogInfo setServerId(long serverId) {
        this.serverId = serverId;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public BinlogInfo setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinlogInfo that = (BinlogInfo) o;
        return Objects.equal(serverId, that.serverId) &&
                Objects.equal(binlogPosition, that.binlogPosition) &&
                Objects.equal(eventIndex, that.eventIndex) &&
                Objects.equal(timestamp, that.timestamp) &&
                Objects.equal(binlogFile, that.binlogFile);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serverId, binlogFile, binlogPosition, eventIndex, timestamp);
    }

    @Override
    public String toString() {
        return "BinlogInfo{" +
                "serverId=" + serverId +
                ", binlogFile='" + binlogFile + '\'' +
                ", binlogPosition=" + binlogPosition +
                ", eventIndex=" + eventIndex +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int compareTo(BinlogInfo binlogInfo) {
        if (timestamp != 0 && binlogInfo.getTimestamp() != 0) {
            return Long.valueOf(timestamp).compareTo(binlogInfo.getTimestamp());
        }

        if (serverId == binlogInfo.getServerId()) {
            int binlogFileResult = binlogFile.compareTo(binlogInfo.getBinlogFile());
            if (binlogFileResult < 0) {
                return -1;
            } else if (binlogFileResult == 0) {
                return Long.valueOf(binlogPosition).compareTo(binlogInfo.getBinlogPosition());
            } else {
                return 1;
            }
        }

        throw new RuntimeException("can not compare two binlog info.");
    }

    public String encode() {
        return new StringBuilder()
                .append(timestamp)
                .append("!")
                .append(serverId)
                .append("!")
                .append(binlogFile)
                .append("!")
                .append(binlogPosition)
                .toString();
    }
}