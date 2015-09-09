package com.dianping.puma.comparison.manager.container;

import com.dianping.puma.biz.entity.CheckTaskEntity;

public interface TaskContainer {

	public void create(CheckTaskEntity checkTask);

	public void remove(CheckTaskEntity checkTask);

	public boolean contains(int id);
}
