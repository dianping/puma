package com.dianping.puma.storage.oldchannel;

public abstract class AbstractEventChannel implements EventChannel {

    /*
    protected String database;

    protected Set<String> tables;

    protected boolean withTransaction = true;

    protected boolean withDdl = false;

    protected boolean withDml = true;

    @Override
    public EventChannel withTables(String... tables) {
        this.tables = new HashSet<String>();
        for (String table : tables) {
            this.tables.add(table);
        }
        return this;
    }

    @Override
    public EventChannel withTransaction(boolean transaction) {
        this.withTransaction = transaction;
        return this;
    }

    @Override
    public EventChannel withDdl(boolean ddl) {
        this.withDdl = ddl;
        return this;
    }

    @Override
    public EventChannel withDml(boolean dml) {
        this.withDml = dml;
        return this;
    }

    @Override
    public String[] getTables() {
        return tables.toArray(new String[tables.size()]);
    }

    @Override
    public boolean getTransaction() {
        return withTransaction;
    }

    @Override
    public boolean getDdl() {
        return withDdl;
    }

    @Override
    public boolean getDml() {
        return withDml;
    }*/
}
