/**
 * Project: ${puma-client.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.client;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * 数据变更事件
 * 
 * 这个事件里的所有表的所有行数据变化是属于一个transaction的。
 * </pre>
 * 
 * @author Leo Liang
 * 
 */
public class DataChangedEvent implements Serializable {

	private static final long		serialVersionUID	= -8268178911916142965L;

	private boolean					empty				= Boolean.FALSE;
	private List<TableChangedData>	datas;
	private long					transactionId;

	/**
	 * @param empty
	 * @param datas
	 * @param transactionId
	 */
	public DataChangedEvent(boolean empty, List<TableChangedData> datas, long transactionId) {
		super();
		this.empty = empty;
		this.datas = datas;
		this.transactionId = transactionId;
	}

	/**
	 * @return the transactionId
	 */
	public long getTransactionId() {
		return transactionId;
	}

	/**
	 * @return the datas
	 */
	public List<TableChangedData> getDatas() {
		return datas;
	}

	/**
	 * @return the empty
	 */
	public boolean isEmpty() {
		return empty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataChangedEvent [empty=" + empty + ", datas=" + datas + ", transactionId=" + transactionId + "]";
	}

}
