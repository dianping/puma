/**
 * Project: ${puma-datahandler.aid}
 * 
 * File Created at 2012-7-3
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
package com.dianping.puma.datahandler;

import com.dianping.puma.core.event.ChangedEvent;

import java.io.Serializable;

/**
 * 
 * @author Leo Liang
 * 
 */
public class DataHandlerResult implements Serializable {

	private static final long serialVersionUID = 8537161008838420062L;

	private ChangedEvent data = null;

	private boolean empty = false;

	private boolean finished = false;

	/**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @param finished
	 *           the finished to set
	 */
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
	 * @return the data
	 */
	public ChangedEvent getData() {
		return data;
	}

	/**
	 * @param data
	 *           the data to set
	 */
	public void setData(ChangedEvent data) {
		this.data = data;
	}

	/**
	 * @return the empty
	 */
	public boolean isEmpty() {
		return empty;
	}

	/**
	 * @param empty
	 *           the empty to set
	 */
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

}
