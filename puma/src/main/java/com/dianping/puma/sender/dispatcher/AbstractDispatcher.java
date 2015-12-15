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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * TODO Comment of AbstractDispatcher
 *
 * @author Leo Liang
 */
public abstract class AbstractDispatcher implements Dispatcher {
    private String name;

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.common.LifeCycle#start()
     */
    @Override
    public void start() {

    }

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.common.LifeCycle#stop()
     */
    @Override
    public void stop() {

    }

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.sender.dispatcher.Dispatcher#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void throwExceptionIfNeeded(List<Throwable> exceptionList) throws DispatcherException {

        if (exceptionList != null && !exceptionList.isEmpty()) {
            StringWriter buffer = new StringWriter();
            PrintWriter out = null;
            try {
                out = new PrintWriter(buffer);

                for (Throwable exception : exceptionList) {
                    exception.printStackTrace(out);
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            throw new DispatcherException(buffer.toString());
        }

    }

}
