package com.dianping.puma.comparison.fetcher;

import com.google.common.base.Strings;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class UpdateTimeAndIdSourceFetcher extends AbstractDataFetcher implements SourceFetcher {

    private String idName = "ID";

    private String updateTimeName = "UpdateTime";

    private Date startTime;

    private Date endTime;

    private String sql;

    private long lastId;

    private int pageSize = 1000;

    @Override
    public void setStartTime(Date time) {
        this.startTime = time;
    }

    @Override
    public void setEndTime(Date time) {
        this.endTime = time;
    }

    @Override
    public List<Map<String, Object>> fetch() {
        initSql();

        List<Map<String, Object>> rows;
        rows = template.queryForList(sql, startTime, endTime, lastId);
        if (rows.size() != 0) {
            lastId = ((Number) rows.get(rows.size() - 1).get(idName)).longValue();
            startTime = (Date) rows.get(rows.size() - 1).get(updateTimeName);
        }
        return rows;
    }

    protected void initSql() {
        if (Strings.isNullOrEmpty(this.sql)) {
            this.sql = String.format(
                    "SELECT %s FROM %s WHERE %s >= ? and %s < ? and %s > ? ORDER BY %s,%s limit %d",
                    columns, tableName, updateTimeName, updateTimeName, idName, updateTimeName, idName, pageSize);
        }
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public String getUpdateTimeName() {
        return updateTimeName;
    }

    public void setUpdateTimeName(String updateTimeName) {
        this.updateTimeName = updateTimeName;
    }
}