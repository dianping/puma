package com.dianping.puma.server.container;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.server.TaskExecutor;

import java.util.List;

public interface TaskContainer {

    public void init();

    public TaskExecutor get(String taskId);

    public List<TaskExecutor> getAll();

    public void create(String name, PumaTaskEntity task);

    public void update(String name, PumaTaskEntity task);

    public void delete(String name, PumaTaskEntity task);

    public void start(String name, PumaTaskEntity task);

    public void stop(String name, PumaTaskEntity task);
}
