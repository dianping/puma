package com.dianping.puma.syncserver.task.container;

import com.dianping.puma.biz.entity.BaseTaskEntity;

public interface TaskContainer<T extends BaseTaskEntity> {

	void create(T task);

	void update(T task);

	void delete(T task);

	void start(T task);

	void stop(T task);
}
