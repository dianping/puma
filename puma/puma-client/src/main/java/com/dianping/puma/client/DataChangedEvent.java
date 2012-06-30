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

	private List<TableChangedData>	datas;
	private long					transactionId;
	private boolean					ddl;
	private String					sql;
	private String					binlogFileName;
	private long					binlogPos;
	private boolean					empty				= true;

	/**
	 * @return the empty
	 */
	public boolean isEmpty() {
		return empty;
	}

	/**
	 * @param empty
	 *            the empty to set
	 */
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	/**
	 * @return the binlogFileName
	 */
	public String getBinlogFileName() {
		return binlogFileName;
	}

	/**
	 * @param binlogFileName
	 *            the binlogFileName to set
	 */
	public void setBinlogFileName(String binlogFileName) {
		this.binlogFileName = binlogFileName;
	}

	/**
	 * @return the binlogPos
	 */
	public long getBinlogPos() {
		return binlogPos;
	}

	/**
	 * @param binlogPos
	 *            the binlogPos to set
	 */
	public void setBinlogPos(long binlogPos) {
		this.binlogPos = binlogPos;
	}

	/**
	 * @return the ddl
	 */
	public boolean isDdl() {
		return ddl;
	}

	/**
	 * @param ddl
	 *            the ddl to set
	 */
	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql
	 *            the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @param datas
	 *            the datas to set
	 */
	public void setDatas(List<TableChangedData> datas) {
		this.datas = datas;
	}

	/**
	 * @param transactionId
	 *            the transactionId to set
	 */
	public void setTransactionId(long transactionId) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataChangedEvent [datas=" + datas + ", transactionId=" + transactionId + ", ddl=" + ddl + ", sql="
				+ sql + ", binlogFileName=" + binlogFileName + ", binlogPos=" + binlogPos + ", empty=" + empty + "]";
	}

}
