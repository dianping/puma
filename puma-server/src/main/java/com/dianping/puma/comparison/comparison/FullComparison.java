package com.dianping.puma.comparison.comparison;

import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class FullComparison implements Comparison {
    @Override
    public boolean compare(Map<String, Object> source, Map<String, Object> target) {
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
}
