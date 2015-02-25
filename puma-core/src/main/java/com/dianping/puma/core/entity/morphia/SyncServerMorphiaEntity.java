package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SyncServerEntity;
import com.google.code.morphia.annotations.Entity;

@Entity("SyncServer")
public class SyncServerMorphiaEntity extends BaseMorphiaEntity<SyncServerEntity> {

	public SyncServerMorphiaEntity() {}

	public SyncServerMorphiaEntity(SyncServerEntity syncServerEntity) {
		super(syncServerEntity);
	}
}
