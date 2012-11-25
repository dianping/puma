package com.dianping.puma.core.sync;

public class SyncSrc {

    private String pumaServerHost;
    private BinlogInfo binlogInfo;
    private String name;
    private Long serverId;
    private String target;
    private Boolean transaction;
    private Boolean ddl;
    private Boolean dml;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((binlogInfo == null) ? 0 : binlogInfo.hashCode());
        result = prime * result + ((ddl == null) ? 0 : ddl.hashCode());
        result = prime * result + ((dml == null) ? 0 : dml.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pumaServerHost == null) ? 0 : pumaServerHost.hashCode());
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((transaction == null) ? 0 : transaction.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SyncSrc))
            return false;
        SyncSrc other = (SyncSrc) obj;
        if (binlogInfo == null) {
            if (other.binlogInfo != null)
                return false;
        } else if (!binlogInfo.equals(other.binlogInfo))
            return false;
        if (ddl == null) {
            if (other.ddl != null)
                return false;
        } else if (!ddl.equals(other.ddl))
            return false;
        if (dml == null) {
            if (other.dml != null)
                return false;
        } else if (!dml.equals(other.dml))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (pumaServerHost == null) {
            if (other.pumaServerHost != null)
                return false;
        } else if (!pumaServerHost.equals(other.pumaServerHost))
            return false;
        if (serverId == null) {
            if (other.serverId != null)
                return false;
        } else if (!serverId.equals(other.serverId))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (transaction == null) {
            if (other.transaction != null)
                return false;
        } else if (!transaction.equals(other.transaction))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SyncSrc [pumaServerHost=" + pumaServerHost + ", binlogInfo=" + binlogInfo + ", name=" + name + ", serverId="
                + serverId + ", target=" + target + ", transaction=" + transaction + ", ddl=" + ddl + ", dml=" + dml + "]";
    }

}
