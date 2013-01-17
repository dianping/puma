package com.dianping.puma.admin.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState.State;

public interface SyncTaskService {

    /**
     * 创建SyncTaskAction，同时创建SyncTaskActionState
     */
    Long create(SyncTask syncTask);

    SyncTask find(Long objectId);

    List<SyncTask> find(int offset, int limit);

    boolean existsBySrcAndDest(String srcMysqlName, String destMysqlName);

    MysqlMapping compare(MysqlMapping oldMysqlMapping, MysqlMapping newMysqlMapping);

    DumpMapping convertMysqlMappingToDumpMapping(MysqlHost mysqlHost, MysqlMapping mysqlMapping) throws SQLException;

    void updateState(Long id, State state, Map<String, String> params);

}
