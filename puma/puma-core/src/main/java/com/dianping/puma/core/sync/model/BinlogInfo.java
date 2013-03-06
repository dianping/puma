package com.dianping.puma.core.sync.model;

public class BinlogInfo {
    private String binlogFile;
    private long binlogPosition;
    /** 是否需要从下一个事件pos开始，如果是，则需要skip所有事件直到遇到第一个非binlogPosition<br> 
     * 对于admin端创建的SyncTask,不需要skip；对于PumaClient记录的binlog，需要skip
     * */
    private boolean skipToNextPos = false;

    public String getBinlogFile() {
        return binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    public long getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public boolean isSkipToNextPos() {
        return skipToNextPos;
    }

    public void setSkipToNextPos(boolean skipToNextPos) {
        this.skipToNextPos = skipToNextPos;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((binlogFile == null) ? 0 : binlogFile.hashCode());
        result = prime * result + (int) (binlogPosition ^ (binlogPosition >>> 32));
        result = prime * result + (skipToNextPos ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BinlogInfo))
            return false;
        BinlogInfo other = (BinlogInfo) obj;
        if (binlogFile == null) {
            if (other.binlogFile != null)
                return false;
        } else if (!binlogFile.equals(other.binlogFile))
            return false;
        if (binlogPosition != other.binlogPosition)
            return false;
        if (skipToNextPos != other.skipToNextPos)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BinlogInfo [binlogFile=" + binlogFile + ", binlogPosition=" + binlogPosition + ", next=" + skipToNextPos + "]";
    }

}
