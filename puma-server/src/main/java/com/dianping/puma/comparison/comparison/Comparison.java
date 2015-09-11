package com.dianping.puma.comparison.comparison;

import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface Comparison {
    boolean compare(Map<String, Object> source, Map<String, Object> target);
}