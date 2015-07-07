package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.DstDBInstance;
import com.google.code.morphia.annotations.Entity;

@Entity("DstDBInstance_")
public class DstDBInstanceMorphia extends BaseMorphiaEntity<DstDBInstance> {

	public DstDBInstanceMorphia() { }

	public DstDBInstanceMorphia(DstDBInstance dstDbInstance) {
		super(dstDbInstance);
	}
}
