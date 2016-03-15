package com.dianping.puma.common.model.message;

/**
 * Created by xiaotian.li on 16/3/12.
 * Email: lixiaotian07@gmail.com
 */
public class EventSubscribeRequest extends EventRequest {

    private String database;

    private String tableRegex;

    private boolean dml;

    private boolean ddl;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableRegex() {
        return tableRegex;
    }

    public void setTableRegex(String tableRegex) {
        this.tableRegex = tableRegex;
    }

    public boolean isDml() {
        return dml;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    public boolean isDdl() {
        return ddl;
    }

    public void setDdl(boolean ddl) {
        this.ddl = ddl;
    }
}
