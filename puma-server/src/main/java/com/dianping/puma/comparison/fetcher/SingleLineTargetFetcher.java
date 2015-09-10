package com.dianping.puma.comparison.fetcher;

import com.dianping.puma.comparison.model.SourceTargetPair;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
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
    public List<Map<String, Object>> fetch(List<Map<String, Object>> source) {
        LinkedHashMap<String, Object> condition = Maps.newLinkedHashMap(source.get(0));

        String sql = String.format("select %s from %s where %s limit 1",
                columns, tableName,
                Joiner.on(" and ").join(FluentIterable.from(condition.keySet()).transform(new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return input + " = ?";
                    }
                })));
        Object[] args = condition.values().toArray(new Object[condition.size()]);
        return template.queryForList(sql, args);
    }

    @Override
    public List<SourceTargetPair> map(List<Map<String, Object>> source, List<Map<String, Object>> target) {
        return Lists.newArrayList(new SourceTargetPair(null, target.size() > 0 ? target.get(0) : null));
    }
}
