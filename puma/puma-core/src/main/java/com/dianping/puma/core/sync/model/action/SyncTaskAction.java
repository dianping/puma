package com.dianping.puma.core.sync.model.action;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.google.code.morphia.annotations.Entity;

@Entity
public class SyncTaskAction extends Action {

    //    源：BinlogInfo起点
    private BinlogInfo binlogInfo;
    //    name：PumaClient的name属性
    private String pumaClientName;
    //    serverId：PumaClient的serverId属性
    private long serverId;
    //    ddl：PumaClient的ddl属性
    private boolean ddl;
    //    dml：PumaClient的dml属性
    private boolean dml;
    //    transaction：PumaClient的transaction属性
    private boolean transaction;
    //    同步配置：映射配置(TableMapping)
    private MysqlMapping mysqlMapping;

    public SyncTaskAction() {
        super(ActionType.SYNC);
    }

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }

    public String getPumaClientName() {
        return pumaClientName;
    }

    public void setPumaClientName(String pumaClientName) {
        this.pumaClientName = pumaClientName;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public boolean isDdl() {
        return ddl;
    }

    public void setDdl(boolean ddl) {
        this.ddl = ddl;
    }

    public boolean isDml() {
        return dml;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public MysqlMapping getMysqlMapping() {
        return mysqlMapping;
    }

    public void setMysqlMapping(MysqlMapping mysqlMapping) {
        this.mysqlMapping = mysqlMapping;
    }

}
