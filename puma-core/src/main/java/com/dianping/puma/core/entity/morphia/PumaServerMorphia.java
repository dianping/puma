package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaServer;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaServer")
public class PumaServerMorphia extends BaseMorphiaEntity<PumaServer> {

	public PumaServerMorphia() {}

	public PumaServerMorphia(PumaServer pumaServer) {
		super(pumaServer);
	}
}
