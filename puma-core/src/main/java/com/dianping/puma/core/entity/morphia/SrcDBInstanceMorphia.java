package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SrcDBInstance;
import com.google.code.morphia.annotations.Entity;

@Entity("SrcDBInstance")
public class SrcDBInstanceMorphia extends BaseMorphiaEntity<SrcDBInstance> {

	public SrcDBInstanceMorphia() { }

	public SrcDBInstanceMorphia(SrcDBInstance srcDbInstance) {
		super(srcDbInstance);
	}
}
