package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SrcDBInstanceEntity;
import com.google.code.morphia.annotations.Entity;

@Entity("SrcDBInstance")
public class SrcDBInstanceMorphiaEntity extends BaseMorphiaEntity<SrcDBInstanceEntity> {

	public SrcDBInstanceMorphiaEntity() { }

	public SrcDBInstanceMorphiaEntity(SrcDBInstanceEntity srcDbInstanceEntity) {
		super(srcDbInstanceEntity);
	}
}
