package com.dianping.puma.checkserver.model;

import java.util.List;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskResult {

    private String cursor;

    private List<SourceTargetPair> difference;

    public List<SourceTargetPair> getDifference() {
        return difference;
    }

    public TaskResult setDifference(List<SourceTargetPair> difference) {
        this.difference = difference;
        return this;
    }

    public String getCursor() {
        return cursor;
    }

    public TaskResult setCursor(String cursor) {
        this.cursor = cursor;
        return this;
    }
}
