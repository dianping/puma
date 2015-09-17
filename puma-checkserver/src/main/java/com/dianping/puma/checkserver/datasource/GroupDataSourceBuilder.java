package com.dianping.puma.checkserver.datasource;

import com.dianping.zebra.group.jdbc.GroupDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

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
        GroupDataSource ds = null;
        try {
            ds = new GroupDataSource();
            ds.setJdbcRef(this.jdbcRef);
            ds.init();
            return ds;
        } catch (RuntimeException e) {
            if (ds != null) {
                try {
                    ds.close();
                } catch (SQLException ignore) {
                }
            }
            throw e;
        }
    }

    @Override
    public void destory(DataSource ds) {
        if (ds instanceof GroupDataSource) {
            try {
                ((GroupDataSource) ds).close();
            } catch (SQLException ignore) {
            }
        }
    }
}