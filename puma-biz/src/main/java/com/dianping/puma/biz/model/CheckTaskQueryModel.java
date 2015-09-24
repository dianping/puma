package com.dianping.puma.biz.model;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CheckTaskQueryModel {
    private String taskName;
    private String taskGroupName;
    private Boolean success;
    private Boolean diffs;

    public Boolean getDiffs() {
        return diffs;
    }

    public void setDiffs(Boolean diffs) {
        this.diffs = diffs;
    }

    public String getTaskName() {
        return taskName;
    }

    public CheckTaskQueryModel setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    public String getTaskGroupName() {
        return taskGroupName;
    }

    public CheckTaskQueryModel setTaskGroupName(String taskGroupName) {
        this.taskGroupName = taskGroupName;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public CheckTaskQueryModel setSuccess(Boolean success) {
        this.success = success;
        return this;
    }
}
