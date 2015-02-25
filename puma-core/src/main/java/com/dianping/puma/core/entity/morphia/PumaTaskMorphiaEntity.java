package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.PumaTaskEntity;
import org.springframework.stereotype.Service;

@Service("PumaTask")
public class PumaTaskMorphiaEntity extends BaseMorphiaEntity<PumaTaskEntity> {

	public PumaTaskMorphiaEntity() {}

	public PumaTaskMorphiaEntity(PumaTaskEntity pumaTaskEntity) {
		super(pumaTaskEntity);
	}
}
