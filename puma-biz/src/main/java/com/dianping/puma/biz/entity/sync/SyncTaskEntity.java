package com.dianping.puma.biz.entity.sync;

import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.entity.sync.BaseTaskEntity;
import com.dianping.puma.biz.entity.sync.mapping.DatabaseMapping;

import java.util.List;

public class SyncTaskEntity extends BaseTaskEntity {

	private int id;

	private String name;

	/** relations: sync servers. */
	private List<SyncServerEntity> syncServers;

	/** relations: puma servers. */
	private List<PumaServerEntity> pumaServers;

	/** relations: destination database instances. */
	private List<DstDbEntity> dstDbs;

	/** relations: source database instances. */
	private List<SrcDbEntity> srcDbs;

	/** relations: database mappings. */
	private List<DatabaseMapping> mappings;

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
}
