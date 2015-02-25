package com.dianping.puma.core.entity.morphia;

import com.dianping.puma.core.entity.BaseEntity;
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;
import org.bson.types.ObjectId;

public class BaseMorphiaEntity<T> {

	@Id
	ObjectId objectId;

	@Indexed(value = IndexDirection.ASC, name = "uniqueId", unique = true, dropDups = true)
	String uniqueId;

	@Embedded
	T entity;

	public BaseMorphiaEntity() {}

	public BaseMorphiaEntity(T entity) {
		this.uniqueId = ((BaseEntity) entity).getId();
		this.entity = entity;
	}

	public T getEntity() {
		return this.entity;
	}
}
