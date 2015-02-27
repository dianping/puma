package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaTaskEntity;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaTask")
public class PumaTaskMorphiaEntity extends BaseMorphiaEntity<PumaTaskEntity> {

	public PumaTaskMorphiaEntity() {}

	public PumaTaskMorphiaEntity(PumaTaskEntity pumaTaskEntity) {
		super(pumaTaskEntity);
	}
}
