package com.dianping.puma.comparison.datasource;

import javax.sql.DataSource;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 *
 * 数据源 Builder，根据配置生成对应的数据源
 */
public interface DataSourceBuilder {
    DataSource build();
}
