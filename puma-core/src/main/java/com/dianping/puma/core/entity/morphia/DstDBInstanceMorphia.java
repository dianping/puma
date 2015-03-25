package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.DstDBInstance;
import com.google.code.morphia.annotations.Entity;

@Entity("DstDBInstance_")
public class DstDBInstanceMorphia extends BaseMorphiaEntity<DstDBInstance> {

	public DstDBInstanceMorphia() { }

	public DstDBInstanceMorphia(DstDBInstance dstDbInstance) {
		super(dstDbInstance);
	}
}
