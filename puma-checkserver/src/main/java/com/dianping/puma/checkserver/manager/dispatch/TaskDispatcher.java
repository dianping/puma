package com.dianping.puma.checkserver.manager.dispatch;

import com.dianping.puma.biz.entity.CheckTaskEntity;

import java.util.List;

public interface TaskDispatcher {

	public void dispatch(List<CheckTaskEntity> checkTasks);
}
