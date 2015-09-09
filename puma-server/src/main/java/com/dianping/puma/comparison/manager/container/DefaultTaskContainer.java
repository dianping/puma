package com.dianping.puma.comparison.manager.container;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskContainer implements TaskContainer {

	@Override
	public void create(CheckTaskEntity checkTask) {

	}

	@Override
	public void remove(CheckTaskEntity checkTask) {

	}

	@Override
	public boolean contains(int id) {
		return false;
	}
}
