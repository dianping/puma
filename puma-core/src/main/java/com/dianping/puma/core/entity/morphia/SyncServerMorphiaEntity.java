package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SyncServer;
import com.google.code.morphia.annotations.Entity;

@Entity("SyncServer")
public class SyncServerMorphiaEntity extends BaseMorphiaEntity<SyncServer> {

	public SyncServerMorphiaEntity() {}

	public SyncServerMorphiaEntity(SyncServer syncServer) {
		super(syncServer);
	}
}
