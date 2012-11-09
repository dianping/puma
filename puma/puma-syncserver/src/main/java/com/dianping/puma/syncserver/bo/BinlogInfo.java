package com.dianping.puma.syncserver.bo;

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

}
