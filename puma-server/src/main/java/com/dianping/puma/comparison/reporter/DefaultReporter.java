package com.dianping.puma.comparison.reporter;

import com.dianping.puma.core.util.GsonUtil;

import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultReporter implements Reporter {
    @Override
    public void report(Map<String, Object> source, Map<String, Object> target) {
        System.out.println(GsonUtil.toJson(source));
    }
}
