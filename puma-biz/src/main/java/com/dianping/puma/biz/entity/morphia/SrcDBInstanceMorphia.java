package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.SrcDBInstance;
import com.google.code.morphia.annotations.Entity;

@Entity("SrcDBInstance_")
public class SrcDBInstanceMorphia extends BaseMorphiaEntity<SrcDBInstance> {

	public SrcDBInstanceMorphia() { }

	public SrcDBInstanceMorphia(SrcDBInstance srcDbInstance) {
		super(srcDbInstance);
	}
}
