package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.PumaServer;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaServer_")
public class PumaServerMorphia extends BaseMorphiaEntity<PumaServer> {

	public PumaServerMorphia() {}

	public PumaServerMorphia(PumaServer pumaServer) {
		super(pumaServer);
	}
}
