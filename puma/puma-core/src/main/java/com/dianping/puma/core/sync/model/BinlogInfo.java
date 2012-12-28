package com.dianping.puma.core.sync.model;

public class BinlogInfo {
    protected String binlogFile;
    protected long binlogPosition;

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

    @Override
    public String toString() {
        return "BinlogPos [BinlogFile=" + binlogFile + ", BinlogPosition=" + binlogPosition + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((binlogFile == null) ? 0 : binlogFile.hashCode());
        result = prime * result + (int) (binlogPosition ^ (binlogPosition >>> 32));
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
        return true;
    }

}
