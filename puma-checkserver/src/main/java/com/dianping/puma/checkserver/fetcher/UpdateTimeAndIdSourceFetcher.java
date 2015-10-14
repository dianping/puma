package com.dianping.puma.checkserver.fetcher;

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

    private String sql;

    private Object lastId;

    private static final int PAGE_SIZE = 1000;

    @Override
    public void setCursor(String cursor) {
        try {
            String[] data = cursor.split(",");
            startTime = new Date(Long.valueOf(data[0]));
            lastId = data.length >= 2 ? data[1] : null;
        } catch (RuntimeException ignore) {
            startTime = new Date(1);
            lastId = null;
        }
    }

    @Override
    public String getCursor() {
        String result = String.valueOf(startTime.getTime());
        if (lastId != null) {
            result += ",";
            result += String.valueOf(lastId);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> fetch() {
        initSql();

        List<Map<String, Object>> rows;
        rows = template.queryForList(sql, startTime, startTime, lastId, lastId, getOneHourAgo());
        if (rows.size() != 0) {
            lastId = rows.get(rows.size() - 1).get(idName);
            startTime = (Date) rows.get(rows.size() - 1).get(updateTimeName);
        }
        return rows;
    }

    private Date getOneHourAgo() {
        return new Date(new Date().getTime() - 60 * 60 * 1000);
    }

    @Override
    public Map<String, Object> retry(Map<String, Object> source) {
        LinkedHashMap<String, Object> condition = Maps.newLinkedHashMap(source);

        String sql = String.format("/*+zebra:w*/select %s from %s where %s limit 1",
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
                    "/*+zebra:w*/SELECT %s FROM %s WHERE"
                            + " ( %s > ? OR (%s = ? AND (? is null OR %s > ?)))"
                            + " AND %s < ? "
                            + " ORDER BY %s,%s limit %d",
                    columns, tableName, updateTimeName, updateTimeName, idName, updateTimeName, updateTimeName, idName, PAGE_SIZE);
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