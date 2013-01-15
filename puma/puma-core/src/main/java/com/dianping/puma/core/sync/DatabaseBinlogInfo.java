package com.dianping.puma.core.sync;

import com.dianping.puma.core.sync.model.BinlogInfo;

public class DatabaseBinlogInfo extends BinlogInfo {

    private String databaseName;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((databaseName == null) ? 0 : databaseName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof DatabaseBinlogInfo))
            return false;
        DatabaseBinlogInfo other = (DatabaseBinlogInfo) obj;
        if (databaseName == null) {
            if (other.databaseName != null)
                return false;
        } else if (!databaseName.equals(other.databaseName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DatabaseBinlogInfo [databaseName=" + databaseName + ", binlogFile=" + binlogFile + ", binlogPosition="
                + binlogPosition + "]";
    }

}
