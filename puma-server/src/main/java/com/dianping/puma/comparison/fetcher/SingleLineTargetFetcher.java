package com.dianping.puma.comparison.fetcher;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SingleLineTargetFetcher implements TargetFetcher {
    @Override
    public List<Map<String, Object>> fetch(List<Map<String, Object>> source) {
        return null;
    }

    @Override
    public List<SourceTargetPair> map(List<Map<String, Object>> source, List<Map<String, Object>> target) {
        return Lists.newArrayList(new SourceTargetPair(source.get(0), target.size() > 0 ? target.get(0) : null));
    }
}
