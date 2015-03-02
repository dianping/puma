package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaTask;
import com.google.code.morphia.annotations.Entity;

@Entity("PumaTask")
public class PumaTaskMorphiaEntity extends BaseMorphiaEntity<PumaTask> {

	public PumaTaskMorphiaEntity() {}

	public PumaTaskMorphiaEntity(PumaTask pumaTask) {
		super(pumaTask);
	}
}
