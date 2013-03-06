package com.dianping.puma.core.sync.model.task;

import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.google.code.morphia.annotations.Entity;

@Entity
public class AbstractTask extends Task {

    private static final long serialVersionUID = 2359517002901314187L;
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

    public AbstractTask(Type type) {
        super(type);
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
