package com.dianping.puma.comparison;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskEntity {

    private String targetDataSourceBuilder;

    private String targetDataSourceBuilderProp;

    private String sourceDataSourceBuilder;

    private String sourceDataSourceBuilderProp;

    public String getSourceDataSourceBuilder() {
        return sourceDataSourceBuilder;
    }

    public void setSourceDataSourceBuilder(String sourceDataSourceBuilder) {
        this.sourceDataSourceBuilder = sourceDataSourceBuilder;
    }

    public String getSourceDataSourceBuilderProp() {
        return sourceDataSourceBuilderProp;
    }

    public void setSourceDataSourceBuilderProp(String sourceDataSourceBuilderProp) {
        this.sourceDataSourceBuilderProp = sourceDataSourceBuilderProp;
    }

    public String getTargetDataSourceBuilder() {
        return targetDataSourceBuilder;
    }

    public void setTargetDataSourceBuilder(String targetDataSourceBuilder) {
        this.targetDataSourceBuilder = targetDataSourceBuilder;
    }

    public String getTargetDataSourceBuilderProp() {
        return targetDataSourceBuilderProp;
    }

    public void setTargetDataSourceBuilderProp(String targetDataSourceBuilderProp) {
        this.targetDataSourceBuilderProp = targetDataSourceBuilderProp;
    }
}
