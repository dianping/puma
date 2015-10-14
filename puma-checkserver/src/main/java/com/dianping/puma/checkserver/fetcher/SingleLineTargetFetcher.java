package com.dianping.puma.checkserver.fetcher;

import com.dianping.puma.checkserver.model.SourceTargetPair;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SingleLineTargetFetcher extends AbstractDataFetcher implements TargetFetcher {
    @Override
    public boolean isBatch() {
        return false;
    }

    @Override
    public SourceTargetPair fetch(Map<String, Object> source, Map<String, Object> c) {
        LinkedHashMap<String, Object> condition = Maps.newLinkedHashMap(c);
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
        return new SourceTargetPair(source, result.size() > 0 ? result.get(0) : null);
    }

    @Override
    public List<SourceTargetPair> fetch(List<Map<String, Object>> source, List<Map<String, Object>> condition) {
        throw new IllegalAccessError(this.getClass().getName() + " not support batched fetch!");
    }
}
