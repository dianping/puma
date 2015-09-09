package com.dianping.puma.comparison.fetcher;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
abstract class AbstractDataFetcher implements DataFetcher {

    protected JdbcTemplate template;

    protected String columns;

    protected String tableName;

    @Override
    public void init(DataSource dataSource) {
        template = new JdbcTemplate(dataSource);
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
