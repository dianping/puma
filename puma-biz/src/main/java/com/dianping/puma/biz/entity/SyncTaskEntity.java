package com.dianping.puma.biz.entity;

import com.dianping.puma.core.dto.mapping.MysqlMapping;

import java.util.List;

public class SyncTaskEntity extends BaseTaskEntity {

	private int mappingId;
	private MysqlMapping mapping;

	private List<Integer> syncServerIds;
	private List<SyncServerEntity> syncServers;

	private List<Integer> dstDbIds;
	private List<DstDbEntity> dstDbs;
}
