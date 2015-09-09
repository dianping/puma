package com.dianping.puma.comparison.fetcher;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class UpdateTimeAndIdSourceFetcher extends AbstractDataFetcher implements SourceFetcher {

    @Override
    public void setStartTime(Date time) {

    }

    @Override
    public void setEndTime(Date time) {

    }

    @Override
    public void init(DataSource dataSource) {

    }

    @Override
    public List<Map<String, Object>> fetch() {
        return null;
    }
}