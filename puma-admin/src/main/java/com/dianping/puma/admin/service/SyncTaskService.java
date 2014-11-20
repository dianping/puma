package com.dianping.puma.admin.service;

import java.sql.SQLException;
import java.util.List;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.SyncTaskStatusAction;

public interface SyncTaskService {

    Long create(SyncTask syncTask);

    SyncTask find(Long objectId);

    List<SyncTask> find(int offset, int limit);

    boolean existsBySrcAndDest(String srcMysqlName, String destMysqlName);

    MysqlMapping compare(MysqlMapping oldMysqlMapping, MysqlMapping newMysqlMapping) throws CloneNotSupportedException;

    DumpMapping convertMysqlMappingToDumpMapping(MysqlHost mysqlHost, MysqlMapping mysqlMapping) throws SQLException;

    void modify(Long id, BinlogInfo binlogInfo, MysqlMapping newMysqlMapping);

    void updateStatusAction(Long taskId, SyncTaskStatusAction statusAction);

    List<SyncTask> findAll();

    void delete(long id);

}
