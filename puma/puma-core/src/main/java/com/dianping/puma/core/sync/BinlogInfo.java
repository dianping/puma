package com.dianping.puma.core.sync;

public class BinlogInfo {
    private String BinlogFile;
    private Long BinlogPosition;

    public String getBinlogFile() {
        return BinlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        BinlogFile = binlogFile;
    }

    public Long getBinlogPosition() {
        return BinlogPosition;
    }

    public void setBinlogPosition(Long binlogPosition) {
        BinlogPosition = binlogPosition;
    }

    @Override
    public String toString() {
        return "BinlogPos [BinlogFile=" + BinlogFile + ", BinlogPosition=" + BinlogPosition + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((BinlogFile == null) ? 0 : BinlogFile.hashCode());
        result = prime * result + ((BinlogPosition == null) ? 0 : BinlogPosition.hashCode());
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
        if (BinlogFile == null) {
            if (other.BinlogFile != null)
                return false;
        } else if (!BinlogFile.equals(other.BinlogFile))
            return false;
        if (BinlogPosition == null) {
            if (other.BinlogPosition != null)
                return false;
        } else if (!BinlogPosition.equals(other.BinlogPosition))
            return false;
        return true;
    }

}
