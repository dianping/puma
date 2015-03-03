package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaClientInfoEntity;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaClientInfo")
public class PumaClientInfoMorphiaEntity extends BaseMorphiaEntity<PumaClientInfoEntity>{

	public PumaClientInfoMorphiaEntity() { }

	public PumaClientInfoMorphiaEntity(PumaClientInfoEntity pumaClientInfoEntity) {
		super(pumaClientInfoEntity);
	}
}
