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

    @Override
    public void init(DataSource dataSource) {
        template = new JdbcTemplate(dataSource);
    }

}
