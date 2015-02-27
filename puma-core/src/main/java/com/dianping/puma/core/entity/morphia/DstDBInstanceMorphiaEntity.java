package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.DstDBInstanceEntity;
import com.google.code.morphia.annotations.Entity;

@Entity("DstDBInstance")
public class DstDBInstanceMorphiaEntity extends BaseMorphiaEntity<DstDBInstanceEntity> {

	public DstDBInstanceMorphiaEntity() { }

	public DstDBInstanceMorphiaEntity(DstDBInstanceEntity dstDbInstanceEntity) {
		super(dstDbInstanceEntity);
	}
}
