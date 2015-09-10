package com.dianping.puma.comparison;

import com.dianping.puma.comparison.model.SourceTargetPair;

import java.util.List;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskResult {

    private List<SourceTargetPair> difference;

    public List<SourceTargetPair> getDifference() {
        return difference;
    }

    public void setDifference(List<SourceTargetPair> difference) {
        this.difference = difference;
    }

}
