package com.dianping.puma.comparison.mapper;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

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
    public Map<String, Object> map(Map<String, Object> row) {
        return Maps.filterEntries(row, new Predicate<Map.Entry<String, Object>>() {
            @Override
            public boolean apply(Map.Entry<String, Object> input) {
                return mapKey.contains(input.getKey());
            }
        });
    }
}
