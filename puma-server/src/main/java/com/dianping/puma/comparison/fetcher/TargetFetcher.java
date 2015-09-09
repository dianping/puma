package com.dianping.puma.comparison.fetcher;

import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TargetFetcher {

    List<Map<String, Object>> fetch(List<Map<String, Object>> columns);

    List<SourceTargetPair> map(List<Map<String, Object>> source, List<Map<String, Object>> target);

    class SourceTargetPair {

        public SourceTargetPair(Map<String, Object> source, Map<String, Object> target) {
            this.source = source;
            this.target = target;
        }

        private final Map<String, Object> source;

        private final Map<String, Object> target;

        public Map<String, Object> getSource() {
            return source;
        }

        public Map<String, Object> getTarget() {
            return target;
        }
    }
}
