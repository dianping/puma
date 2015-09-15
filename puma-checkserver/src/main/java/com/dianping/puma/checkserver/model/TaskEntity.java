package com.dianping.puma.checkserver.model;

import java.util.Date;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskEntity {

    private Date beginTime;

    private Date endTime;

    private String comparison;

    private String comparisonProp;

    private String sourceFetcher;

    private String sourceFetcherProp;

    private String targetFetcher;

    private String targetFetcherProp;

    private String mapper;

    private String mapperProp;

    private String targetDsBuilder;

    private String targetDsBuilderProp;

    private String sourceDsBuilder;

    private String sourceDsBuilderProp;

    public String getSourceDsBuilder() {
        return sourceDsBuilder;
    }

    public void setSourceDsBuilder(String sourceDsBuilder) {
        this.sourceDsBuilder = sourceDsBuilder;
    }

    public String getSourceDsBuilderProp() {
        return sourceDsBuilderProp;
    }

    public void setSourceDsBuilderProp(String sourceDsBuilderProp) {
        this.sourceDsBuilderProp = sourceDsBuilderProp;
    }

    public String getTargetDsBuilder() {
        return targetDsBuilder;
    }

    public void setTargetDsBuilder(String targetDsBuilder) {
        this.targetDsBuilder = targetDsBuilder;
    }

    public String getTargetDsBuilderProp() {
        return targetDsBuilderProp;
    }

    public void setTargetDsBuilderProp(String targetDsBuilderProp) {
        this.targetDsBuilderProp = targetDsBuilderProp;
    }

    public String getComparison() {
        return comparison;
    }

    public void setComparison(String comparison) {
        this.comparison = comparison;
    }

    public String getComparisonProp() {
        return comparisonProp;
    }

    public void setComparisonProp(String comparisonProp) {
        this.comparisonProp = comparisonProp;
    }

    public String getSourceFetcher() {
        return sourceFetcher;
    }

    public void setSourceFetcher(String sourceFetcher) {
        this.sourceFetcher = sourceFetcher;
    }

    public String getSourceFetcherProp() {
        return sourceFetcherProp;
    }

    public void setSourceFetcherProp(String sourceFetcherProp) {
        this.sourceFetcherProp = sourceFetcherProp;
    }

    public String getTargetFetcher() {
        return targetFetcher;
    }

    public void setTargetFetcher(String targetFetcher) {
        this.targetFetcher = targetFetcher;
    }

    public String getTargetFetcherProp() {
        return targetFetcherProp;
    }

    public void setTargetFetcherProp(String targetFetcherProp) {
        this.targetFetcherProp = targetFetcherProp;
    }

    public String getMapper() {
        return mapper;
    }

    public void setMapper(String mapper) {
        this.mapper = mapper;
    }

    public String getMapperProp() {
        return mapperProp;
    }

    public void setMapperProp(String mapperProp) {
        this.mapperProp = mapperProp;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
