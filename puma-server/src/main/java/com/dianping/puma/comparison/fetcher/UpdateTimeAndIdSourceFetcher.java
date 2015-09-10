package com.dianping.puma.comparison.fetcher;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import java.util.Date;
import java.util.LinkedHashMap;
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

    private Object lastId;

    private static final int PAGE_SIZE = 1000;

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
        rows = template.queryForList(sql, startTime, endTime, lastId, lastId);
        if (rows.size() != 0) {
            lastId = rows.get(rows.size() - 1).get(idName);
            startTime = (Date) rows.get(rows.size() - 1).get(updateTimeName);
        }
        return rows;
    }

    @Override
    public Map<String, Object> retry(Map<String, Object> source) {
        LinkedHashMap<String, Object> condition = Maps.newLinkedHashMap(source);

        String sql = String.format("select %s from %s where %s limit 1",
                columns, tableName,
                Joiner.on(" and ").join(FluentIterable.from(condition.keySet()).transform(new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return input + " = ?";
                    }
                })));
        Object[] args = condition.values().toArray(new Object[condition.size()]);
        List<Map<String, Object>> result = template.queryForList(sql, args);

        return result.size() > 0 ? result.get(0) : null;
    }

    protected void initSql() {
        if (Strings.isNullOrEmpty(this.sql)) {
            this.sql = String.format(
                    "SELECT %s FROM %s WHERE %s >= ? and %s < ? and (? is null or %s > ?) ORDER BY %s,%s limit %d",
                    columns, tableName, updateTimeName, updateTimeName, idName, updateTimeName, idName, PAGE_SIZE);
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