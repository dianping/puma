package com.dianping.puma.comparison.datasource;

import com.dianping.zebra.group.jdbc.GroupDataSource;

import javax.sql.DataSource;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class GroupDataSourceBuilder implements DataSourceBuilder {

    private String jdbcRef;

    private String tableName;

    public String getJdbcRef() {
        return jdbcRef;
    }

    public void setJdbcRef(String jdbcRef) {
        this.jdbcRef = jdbcRef;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public DataSource build() {
        GroupDataSource ds = new GroupDataSource();
        ds.setJdbcRef(this.jdbcRef);
        ds.init();
        return ds;
    }
}