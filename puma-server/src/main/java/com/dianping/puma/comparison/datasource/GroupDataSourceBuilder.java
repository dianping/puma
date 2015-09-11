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

    public String getJdbcRef() {
        return jdbcRef;
    }

    public void setJdbcRef(String jdbcRef) {
        this.jdbcRef = jdbcRef;
    }

    @Override
    public DataSource build() {
        GroupDataSource ds = new GroupDataSource();
        ds.setJdbcRef(this.jdbcRef);
        ds.init();
        return ds;
    }
}