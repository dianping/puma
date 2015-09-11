package com.dianping.puma.comparison.manager.container;

import com.dianping.puma.biz.entity.CheckTaskEntity;

public interface CheckTaskContainer {

    void create(CheckTaskEntity checkTask);

    void remove(int taskId);

    boolean contains(int taskId);

    int size();
}
