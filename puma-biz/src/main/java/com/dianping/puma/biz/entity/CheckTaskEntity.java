package com.dianping.puma.biz.entity;

import java.util.Date;

public class CheckTaskEntity {

	private int id;

	private Date initTime;

	private Date currTime;

	private Date nextTime;

	private boolean running;

	private String ownerHost;

	private boolean success;

	private String message;

	private Date updateTime;

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getInitTime() {
		return initTime;
	}

	public void setInitTime(Date initTime) {
		this.initTime = initTime;
	}

	public Date getCurrTime() {
		return currTime;
	}

	public void setCurrTime(Date currTime) {
		this.currTime = currTime;
	}

	public Date getNextTime() {
		return nextTime;
	}

	public void setNextTime(Date nextTime) {
		this.nextTime = nextTime;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getOwnerHost() {
		return ownerHost;
	}

	public void setOwnerHost(String ownerHost) {
		this.ownerHost = ownerHost;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
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

	public String getMapper() {
		return mapper;
	}

	public void setMapper(String mapper) {
		this.mapper = mapper;
	}

	public String getTargetFetcherProp() {
		return targetFetcherProp;
	}

	public void setTargetFetcherProp(String targetFetcherProp) {
		this.targetFetcherProp = targetFetcherProp;
	}

	public String getMapperProp() {
		return mapperProp;
	}

	public void setMapperProp(String mapperProp) {
		this.mapperProp = mapperProp;
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

	public String getSourceDsBuilderProp() {
		return sourceDsBuilderProp;
	}

	public void setSourceDsBuilderProp(String sourceDsBuilderProp) {
		this.sourceDsBuilderProp = sourceDsBuilderProp;
	}

	public String getSourceDsBuilder() {
		return sourceDsBuilder;
	}

	public void setSourceDsBuilder(String sourceDsBuilder) {
		this.sourceDsBuilder = sourceDsBuilder;
	}
}
