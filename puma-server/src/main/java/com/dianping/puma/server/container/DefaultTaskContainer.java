package com.dianping.puma.server.container;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.taskexecutor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultTaskContainer implements TaskContainer {

    private ConcurrentHashMap<String, TaskExecutor> taskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

    public static DefaultTaskContainer instance;

    @Autowired
    private TaskBuilder taskBuilder;

    @Override
    public TaskExecutor get(String taskName) {
        return taskExecutors.get(taskName);
    }

    @Override
    public List<TaskExecutor> getAll() {
        return new ArrayList<TaskExecutor>(taskExecutors.values());
    }

    public EventStorage getTaskStorageByTaskName(String taskName) {
        if (taskExecutors.containsKey(taskName)) {
            List<Sender> senders = taskExecutors.get(taskName).getFileSender();
            if (senders != null && senders.size() > 0) {
                return senders.get(0).getStorage();
            }
        }
        return null;
    }

    @Override
    public EventStorage getTaskStorage(String database) {
        for (TaskExecutor taskExecutor : taskExecutors.values()) {
            TableSet tableSet = taskExecutor.getTask().getTableSet();
            List<Table> tables = tableSet.listSchemaTables();

            for (Table table : tables) {
                if (table.getSchemaName().equals(database)) {
                    return getTaskStorageByTaskName(taskExecutor.getTask().getName());
                }
            }
        }

        return null;
    }

    //
    // public Map<String, TaskExecutor> getTaskExecutors() {
    // return Collections.unmodifiableMap(taskExecutors);
    // }
    //
    // public String getPumaServerName() {
    // return pumaServerName;
    // }
    //
    // public boolean canStop(TaskExecutor taskExecutor) {
    // return taskExecutor.getStatus() != Status.STOPPED && taskExecutor.getStatus() != Status.STOPPING
    // && taskExecutor.getStatus() != Status.FAILED;
    // }
    //
    // public boolean canStart(TaskExecutor taskExecutor) {
    // return taskExecutor.getStatus() != Status.RUNNING && taskExecutor.getStatus() != Status.PREPARING;
    // }
    //
    // public void publishAcceptedTableChangedEvent(String name, TableSet tableSet) {
    // AcceptedTableChangedEvent acceptedTableChangedEvent = new AcceptedTableChangedEvent();
    // acceptedTableChangedEvent.setName(name);
    // acceptedTableChangedEvent.setTableSet(tableSet);
    //
    // eventCenter.post(acceptedTableChangedEvent);
    // }

    @Override
    public void create(String taskName, PumaTaskEntity task) {
        try {
            TaskExecutor taskExecutor = taskBuilder.build(task);

            if (taskExecutors.putIfAbsent(taskName, taskExecutor) != null) {
                throw new RuntimeException("create puma task failure, duplicate exists.");
            }

            start(taskName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String taskName, PumaTaskEntity oriTask, PumaTaskEntity task) {

    }

    @Override
    public void delete(String taskName, PumaTaskEntity task) {
        if (taskExecutors.remove(taskName) == null) {
            throw new RuntimeException("delete puma task failure, not exists.");
        }

        stop(taskName);
    }

    @Override
    public void start(String taskName) {

        try {
            TaskExecutor taskExecutor = taskExecutors.get(taskName);
            if (taskExecutor != null) {
                taskExecutor.start();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void stop(String taskName) {
        TaskExecutor taskExecutor = taskExecutors.get(taskName);
        if (taskExecutor == null) {
            throw new RuntimeException("stop puma task failure, not exists.");
        }

        try {
            taskExecutor.stop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}