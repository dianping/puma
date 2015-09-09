package com.dianping.puma.biz.entity;

public class CheckTaskColumnMappingEntity {

	private int id;

	private int taskId;

	private String srcColumn;

	private String dstColumn;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getSrcColumn() {
		return srcColumn;
	}

	public void setSrcColumn(String srcColumn) {
		this.srcColumn = srcColumn;
	}

	public String getDstColumn() {
		return dstColumn;
	}

	public void setDstColumn(String dstColumn) {
		this.dstColumn = dstColumn;
	}
}
