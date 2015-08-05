package com.dianping.puma.syncserver.manager.container;

import com.dianping.puma.biz.entity.BaseTaskEntity;

public interface TaskContainer {

	public void create(String taskName, BaseTaskEntity task);

	public void update(String taskName, BaseTaskEntity oriTask, BaseTaskEntity task);

	public void delete(String taskName, BaseTaskEntity task);

	public void start(String taskName);

	public void stop(String taskName);
}
