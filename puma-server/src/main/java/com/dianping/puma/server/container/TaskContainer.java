package com.dianping.puma.server.container;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.taskexecutor.TaskExecutor;

import java.util.List;

public interface TaskContainer {

    TaskExecutor get(String instanceName);

    List<TaskExecutor> getAll();

    EventStorage getTaskStorage(String database);

    void create(String name, PumaTaskEntity task);

    void update(String name, PumaTaskEntity oriTask, PumaTaskEntity task);

    void delete(String name, PumaTaskEntity task);

    void start(String name);

    void stop(String name);
}
