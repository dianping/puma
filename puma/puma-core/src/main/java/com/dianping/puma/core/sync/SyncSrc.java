package com.dianping.puma.core.sync;

public class SyncSrc {

    private String pumaServerHost;
    private BinlogInfo binlogInfo;
    private String name;
    private long serverId;
    private String target;
    private boolean transaction;
    private boolean ddl;
    private boolean dml;

    public String getPumaServerHost() {
        return pumaServerHost;
    }

    public void setPumaServerHost(String pumaServerHost) {
        this.pumaServerHost = pumaServerHost;
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean getTransaction() {
        return transaction;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public boolean getDdl() {
        return ddl;
    }

    public void setDdl(boolean ddl) {
        this.ddl = ddl;
    }

    public boolean getDml() {
        return dml;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((binlogInfo == null) ? 0 : binlogInfo.hashCode());
        result = prime * result + (ddl ? 1231 : 1237);
        result = prime * result + (dml ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pumaServerHost == null) ? 0 : pumaServerHost.hashCode());
        result = prime * result + (int) (serverId ^ (serverId >>> 32));
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + (transaction ? 1231 : 1237);
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
        if (ddl != other.ddl)
            return false;
        if (dml != other.dml)
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
        if (serverId != other.serverId)
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (transaction != other.transaction)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SyncSrc [pumaServerHost=" + pumaServerHost + ", binlogInfo=" + binlogInfo + ", name=" + name + ", serverId="
                + serverId + ", target=" + target + ", transaction=" + transaction + ", ddl=" + ddl + ", dml=" + dml + "]";
    }

}
