package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaServerEntity;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaServer")
public class PumaServerMorphiaEntity extends BaseMorphiaEntity<PumaServerEntity> {

	public PumaServerMorphiaEntity() {}

	public PumaServerMorphiaEntity(PumaServerEntity pumaServerEntity) {
		super(pumaServerEntity);
	}
}
