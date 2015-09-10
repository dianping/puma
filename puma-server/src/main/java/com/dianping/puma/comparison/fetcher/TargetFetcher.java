package com.dianping.puma.comparison.fetcher;

import com.dianping.puma.comparison.model.SourceTargetPair;

import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TargetFetcher extends DataFetcher {

    boolean isBatch();

    SourceTargetPair fetch(Map<String, Object> source, Map<String, Object> condition);

    List<SourceTargetPair> fetch(List<Map<String, Object>> source, List<Map<String, Object>> condition);

}
