package com.dianping.puma.core.model;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class BinlogInfo implements Serializable {

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

    public BinlogInfo(long timestamp, long serverId, String binlogFile, long binlogPosition) {
        this.timestamp = timestamp;
        this.serverId = serverId;
        this.binlogFile = binlogFile;
        this.binlogPosition = binlogPosition;
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

    public boolean greaterThan(BinlogInfo bBinlogInfo) {
        long aServerId = this.getServerId();
        long bServerId = bBinlogInfo.getServerId();
        if (aServerId != 0 && bServerId != 0 && aServerId == bServerId) {
            String aBinlogFile = this.getBinlogFile();
            String bBinlogFile = bBinlogInfo.getBinlogFile();

            Integer aBinlogFileNumber = Integer.valueOf(StringUtils.substringAfterLast(aBinlogFile, "."));
            Integer bBinlogFileNumber = Integer.valueOf(StringUtils.substringAfterLast(bBinlogFile, "."));
            int result = aBinlogFileNumber.compareTo(bBinlogFileNumber);

            if (result > 0) {
                return true;
            } else if (result < 0) {
                return false;
            } else {
                long aBinlogPosition = this.getBinlogPosition();
                long bBinlogPosition = bBinlogInfo.getBinlogPosition();
                return aBinlogPosition > bBinlogPosition;
            }
        } else {
            long aTimestamp = this.getTimestamp();
            long bTimestamp = bBinlogInfo.getTimestamp();
            if (aTimestamp == 0 || bTimestamp == 0) {
                return true;
            }
            return aTimestamp > bTimestamp;
        }
    }
}