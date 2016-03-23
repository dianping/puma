package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/2/24.
 * Email: lixiaotian07@gmail.com
 */
public class ClientConfigEntity extends BaseEntity {

    private String clientName;

    private String databaseName;

    private String tableRegex;

    private boolean dml;

    private boolean ddl;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableRegex() {
        return tableRegex;
    }

    public void setTableRegex(String tableRegex) {
        this.tableRegex = tableRegex;
    }

    public boolean getDml() {
        return dml;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    public boolean getDdl() {
        return ddl;
    }

    public void setDdl(boolean ddl) {
        this.ddl = ddl;
    }
}
