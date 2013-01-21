/**
 * Project: puma-syncserver
 * 
 * File Created at 2013-1-16
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.syncserver.job;

import java.util.List;

import com.dianping.puma.core.LifeCycle;

/**
 * TODO Comment of JobCheckStrategy
 * 
 * @author Leo Liang
 * 
 */
public interface TaskCheckStrategy extends LifeCycle<TaskExecutionException> {
    public List<TaskExecutor> check() throws TaskExecutionException;

    public String getName();
}
