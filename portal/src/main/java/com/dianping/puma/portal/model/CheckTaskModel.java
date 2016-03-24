package com.dianping.puma.portal.model;

import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CheckTaskModel {

    private BaseInfo baseInfo;

    private Map<String, Object> comparisonProp;

    private Map<String, Object> sourceFetcherProp;

    private Map<String, Object> targetFetcherProp;

    private Map<String, Object> mapperProp;

    private Map<String, Object> targetDsBuilderProp;

    private Map<String, Object> sourceDsBuilderProp;

    public BaseInfo getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(BaseInfo baseInfo) {
        this.baseInfo = baseInfo;
    }

    public Map<String, Object> getComparisonProp() {
        return comparisonProp;
    }

    public void setComparisonProp(Map<String, Object> comparisonProp) {
        this.comparisonProp = comparisonProp;
    }

    public Map<String, Object> getSourceFetcherProp() {
        return sourceFetcherProp;
    }

    public void setSourceFetcherProp(Map<String, Object> sourceFetcherProp) {
        this.sourceFetcherProp = sourceFetcherProp;
    }

    public Map<String, Object> getTargetFetcherProp() {
        return targetFetcherProp;
    }

    public void setTargetFetcherProp(Map<String, Object> targetFetcherProp) {
        this.targetFetcherProp = targetFetcherProp;
    }

    public Map<String, Object> getMapperProp() {
        return mapperProp;
    }

    public void setMapperProp(Map<String, Object> mapperProp) {
        this.mapperProp = mapperProp;
    }

    public Map<String, Object> getTargetDsBuilderProp() {
        return targetDsBuilderProp;
    }

    public void setTargetDsBuilderProp(Map<String, Object> targetDsBuilderProp) {
        this.targetDsBuilderProp = targetDsBuilderProp;
    }

    public Map<String, Object> getSourceDsBuilderProp() {
        return sourceDsBuilderProp;
    }

    public void setSourceDsBuilderProp(Map<String, Object> sourceDsBuilderProp) {
        this.sourceDsBuilderProp = sourceDsBuilderProp;
    }

    public static class BaseInfo {

        private String name;

        private String groupName;

        private String cursor;

        private String batch;

        public String getBatch() {
            return batch;
        }

        public void setBatch(String batch) {
            this.batch = batch;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCursor() {
            return cursor;
        }

        public void setCursor(String cursor) {
            this.cursor = cursor;
        }

        public String getGroupName() {
            return groupName;
        }

        public BaseInfo setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }
    }
}
