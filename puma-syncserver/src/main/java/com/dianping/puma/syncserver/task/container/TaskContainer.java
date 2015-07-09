package com.dianping.puma.syncserver.task.container;

import com.dianping.puma.biz.entity.sync.BaseTaskEntity;

public interface TaskContainer<T extends BaseTaskEntity> {

	void create(T task);

	void update(T task);

	void delete(T task);

	void pause(T task);

	void resume(T task);
}
