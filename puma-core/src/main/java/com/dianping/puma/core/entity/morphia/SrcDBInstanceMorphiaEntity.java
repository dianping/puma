package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.SrcDBInstance;
import com.google.code.morphia.annotations.Entity;

@Entity("SrcDBInstance")
public class SrcDBInstanceMorphiaEntity extends BaseMorphiaEntity<SrcDBInstance> {

	public SrcDBInstanceMorphiaEntity() { }

	public SrcDBInstanceMorphiaEntity(SrcDBInstance srcDbInstance) {
		super(srcDbInstance);
	}
}
