package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SyncServer;
import com.google.code.morphia.annotations.Entity;

@Entity("SyncServer_")
public class SyncServerMorphia extends BaseMorphiaEntity<SyncServer> {

	public SyncServerMorphia() {}

	public SyncServerMorphia(SyncServer syncServer) {
		super(syncServer);
	}
}
