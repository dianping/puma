package com.dianping.puma.admin.service;

import java.sql.SQLException;
import java.util.List;

import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;

public interface SyncTaskActionService {

    /**
     * 创建SyncTaskAction，同时创建SyncTaskActionState
     */
    Long create(SyncTaskAction syncTaskAction);

    SyncTaskAction find(Long objectId);

    List<SyncTaskAction> find(int offset, int limit);

    boolean existsBySrcAndDest(String srcMysqlName, String destMysqlName);

    MysqlMapping compare(MysqlMapping oldMysqlMapping, MysqlMapping newMysqlMapping);

    DumpMapping convertMysqlMappingToDumpMapping(MysqlHost mysqlHost, MysqlMapping mysqlMapping) throws SQLException;

}
