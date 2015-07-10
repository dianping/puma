package com.dianping.puma.biz.entity.sync;

import com.dianping.puma.biz.entity.DstDbEntity;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.entity.SyncServerEntity;
import com.dianping.puma.core.dto.mapping.DatabaseMapping;

import java.util.List;

public class SyncTaskEntity extends BaseTaskEntity {

	private int id;

	private String name;

	private DatabaseMapping mapping;

	/** relations: sync servers. */
	private List<Integer> syncServerIds;
	private List<SyncServerEntity> syncServers;

	/** relations: puma servers. */
	private List<Integer> pumaServerIds;
	private List<PumaServerEntity> pumaServers;

	/** relations: destination database instances. */
	private List<Integer> dstDbIds;
	private List<DstDbEntity> dstDbs;

	/** relations: source database instances. */
	private List<Integer> srcDbIds;
	private List<SrcDbEntity> srcDbs;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DatabaseMapping getMapping() {
		return mapping;
	}

	public void setMapping(DatabaseMapping mapping) {
		this.mapping = mapping;
	}

	public List<Integer> getSyncServerIds() {
		return syncServerIds;
	}

	public void setSyncServerIds(List<Integer> syncServerIds) {
		this.syncServerIds = syncServerIds;
	}

	public List<SyncServerEntity> getSyncServers() {
		return syncServers;
	}

	public void setSyncServers(List<SyncServerEntity> syncServers) {
		this.syncServers = syncServers;
	}

	public List<Integer> getPumaServerIds() {
		return pumaServerIds;
	}

	public void setPumaServerIds(List<Integer> pumaServerIds) {
		this.pumaServerIds = pumaServerIds;
	}

	public List<PumaServerEntity> getPumaServers() {
		return pumaServers;
	}

	public void setPumaServers(List<PumaServerEntity> pumaServers) {
		this.pumaServers = pumaServers;
	}

	public List<Integer> getDstDbIds() {
		return dstDbIds;
	}

	public void setDstDbIds(List<Integer> dstDbIds) {
		this.dstDbIds = dstDbIds;
	}

	public List<DstDbEntity> getDstDbs() {
		return dstDbs;
	}

	public void setDstDbs(List<DstDbEntity> dstDbs) {
		this.dstDbs = dstDbs;
	}

	public List<Integer> getSrcDbIds() {
		return srcDbIds;
	}

	public void setSrcDbIds(List<Integer> srcDbIds) {
		this.srcDbIds = srcDbIds;
	}

	public List<SrcDbEntity> getSrcDbs() {
		return srcDbs;
	}

	public void setSrcDbs(List<SrcDbEntity> srcDbs) {
		this.srcDbs = srcDbs;
	}
}
