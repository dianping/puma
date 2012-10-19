package com.dianping.puma.syncserver.mysql;

import java.util.Arrays;

public class MysqlUpdateStatement {

    private String sql;
    private Object[] args;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "MysqlUpdateStatement [sql=" + sql + ", args=" + Arrays.toString(args) + "]";
    }

}
