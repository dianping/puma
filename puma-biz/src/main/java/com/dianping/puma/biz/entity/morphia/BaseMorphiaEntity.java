package com.dianping.puma.biz.entity.morphia;

import com.dianping.puma.biz.entity.old.BaseEntity;
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

import java.io.Serializable;

public class BaseMorphiaEntity<T extends BaseEntity> implements Serializable {

	private static final long serialVersionUID = 8121775127353895001L;

	@Id
	private Long id;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	String name;

	@Embedded
	T entity;

	public BaseMorphiaEntity() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
		this.entity.setId(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BaseMorphiaEntity(T entity) {
		this.name = ((BaseEntity) entity).getName();
		this.entity = entity;
	}

	public T getEntity() {
		return this.entity;
	}
}
