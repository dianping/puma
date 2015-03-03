package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.DstDBInstance;
import com.google.code.morphia.annotations.Entity;

@Entity("DstDBInstance")
public class DstDBInstanceMorphiaEntity extends BaseMorphiaEntity<DstDBInstance> {

	public DstDBInstanceMorphiaEntity() { }

	public DstDBInstanceMorphiaEntity(DstDBInstance dstDbInstance) {
		super(dstDbInstance);
	}
}
