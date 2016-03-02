/**
 * Project: ${puma-server.aid}
 * <p/>
 * File Created at 2012-6-6 $Id$
 * <p/>
 * Copyright 2010 dianping.com. All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.taskexecutor;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.model.TableSet;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.storage.manage.InstanceStorageManager;
import com.dianping.puma.taskexecutor.task.InstanceTask;

import java.util.List;

/**
 * @author Leo Liang
 */
public interface TaskExecutor extends LifeCycle {

    boolean isStop();

    boolean isMerging();

    void stopUntil(long timestamp);

    void cancelStopUntil();

    void setContext(PumaContext context);

    void initContext();

    PumaContext getContext();

    String getTaskId();

    void setTaskId(String taskId);

    String getTaskName();

    void setTaskName(String taskName);

    String getDefaultBinlogFileName();

    void setDefaultBinlogFileName(String binlogFileName);

    Long getDefaultBinlogPosition();

    void setDefaultBinlogPosition(Long binlogFileName);

    void setInstanceStorageManager(InstanceStorageManager holder);

    List<Sender> getFileSender();

    DataHandler getDataHandler();

    void resume() throws Exception;

    void pause() throws Exception;

    PumaTaskStateEntity getTaskState();

    void setTaskState(PumaTaskStateEntity taskState);

    void setInstanceTask(InstanceTask instanceTask);

    InstanceTask getInstanceTask();

    TableSet getTableSet();
}
