package com.dianping.puma.comparison.mapper;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultRowMapper implements RowMapper {

    private Set<String> mapKey;

    public Set<String> getMapKey() {
        return mapKey;
    }

    public void setMapKey(Set<String> mapKey) {
        this.mapKey = ImmutableSet.<String>builder().addAll(mapKey).build();
    }

    @Override
    public List<Map<String, Object>> mapToTarget(List<Map<String, Object>> source) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        for (Map<String, Object> row : source) {
            result.add(map(row));
        }

        return result;
    }

    @Override
    public Map<String, Object> mapToTarget(Map<String, Object> source) {
        return map(source);
    }

    @Override
    public Map<String, Object> mapToSource(Map<String, Object> source) {
        return map(source);
    }

    protected Map<String, Object> map(Map<String, Object> row) {
        return Maps.filterEntries(row, new Predicate<Map.Entry<String, Object>>() {
            @Override
            public boolean apply(Map.Entry<String, Object> input) {
                return mapKey.contains(input.getKey());
            }
        });
    }
}
