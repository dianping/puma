/**
 * Project: ${puma-sender.aid}
 * <p/>
 * File Created at 2012-6-27
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.sender.dispatcher;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.sender.Sender;

import java.util.List;

/**
 * TODO Comment of Dispatcher
 *
 * @author Leo Liang
 *
 */
public interface Dispatcher extends LifeCycle {
    String getName();

    void dispatch(ChangedEvent event, PumaContext context) throws DispatcherException;

    List<Sender> getSenders();
}
