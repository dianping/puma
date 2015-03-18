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

	@Indexed(value = IndexDirection.ASC, name = "id", unique = true, dropDups = true)
	String id;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	String name;

	@Embedded
	T entity;

	public BaseMorphiaEntity() {}

	public ObjectId getObjectId() {
		return objectId;
	}

	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BaseMorphiaEntity(T entity) {
		this.id = ((BaseEntity) entity).getId();
		this.name = ((BaseEntity) entity).getName();
		this.entity = entity;
	}

	public T getEntity() {
		return this.entity;
	}
}
