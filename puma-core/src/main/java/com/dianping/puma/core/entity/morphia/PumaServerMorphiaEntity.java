package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaServer;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaServer")
public class PumaServerMorphiaEntity extends BaseMorphiaEntity<PumaServer> {

	public PumaServerMorphiaEntity() {}

	public PumaServerMorphiaEntity(PumaServer pumaServer) {
		super(pumaServer);
	}
}
