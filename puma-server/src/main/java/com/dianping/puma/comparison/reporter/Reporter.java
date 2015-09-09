package com.dianping.puma.comparison.reporter;

import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface Reporter {
    void report(Map<String, Object> source, Map<String, Object> target);
}
