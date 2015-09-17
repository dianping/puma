package com.dianping.puma.checkserver.comparison;

import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class FullComparison implements Comparison {

    private Set<String> ignoreColumns;

    private final IgnoreColumnPredicate predicate = new IgnoreColumnPredicate();

    @Override
    public boolean compare(Map<String, Object> source, Map<String, Object> target) {
        if (source == null || target == null) {
            return Objects.equal(source, target);
        }

        if (ignoreColumns != null && ignoreColumns.size() > 0) {
            source = Maps.filterEntries(source, predicate);
            target = Maps.filterEntries(target, predicate);
        }

        MapDifference<String, Object> result = Maps
                .difference(source, target, new Equivalence<Object>() {
                    @Override
                    protected boolean doEquivalent(Object a, Object b) {
                        if (a == null || b == null) {
                            return a == null && b == null ? true : false;
                        }

                        if (a instanceof byte[]) {
                            return Arrays.equals((byte[]) a, (byte[]) b);
                        }

                        return a.equals(b);
                    }

                    @Override
                    protected int doHash(Object o) {
                        return o.hashCode();
                    }
                });
        return result.areEqual();
    }

    public Set<String> getIgnoreColumns() {
        return ignoreColumns;
    }

    public void setIgnoreColumns(Set<String> ignoreColumns) {
        this.ignoreColumns = ignoreColumns;
    }

    class IgnoreColumnPredicate implements Predicate<Map.Entry<String, Object>> {
        @Override
        public boolean apply(Map.Entry<String, Object> input) {
            return !ignoreColumns.contains(input.getKey());
        }
    }
}
