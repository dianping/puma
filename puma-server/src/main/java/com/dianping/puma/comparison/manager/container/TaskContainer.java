package com.dianping.puma.comparison.manager.container;

import com.dianping.puma.biz.entity.CheckTaskEntity;

public interface TaskContainer {

	public void create(CheckTaskEntity checkTask);

	public void remove(int taskId);

	public boolean contains(int taskId);
}
