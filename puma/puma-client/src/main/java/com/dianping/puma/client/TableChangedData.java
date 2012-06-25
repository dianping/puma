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
 * 对应一个表的数据变化
 * </pre>
 * 
 * @author Leo Liang
 * 
 */
public class TableChangedData implements Serializable {

	private static final long	serialVersionUID	= -2383357060735671907L;

	private TableMetaInfo		meta;
	List<RowChangedData>		rows;

	/**
	 * @param meta
	 *            the meta to set
	 */
	public void setMeta(TableMetaInfo meta) {
		this.meta = meta;
	}

	/**
	 * @param rows
	 *            the rows to set
	 */
	public void setRows(List<RowChangedData> rows) {
		this.rows = rows;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the meta
	 */
	public TableMetaInfo getMeta() {
		return meta;
	}

	/**
	 * @return the rows
	 */
	public List<RowChangedData> getRows() {
		return rows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TableChangedData [meta=" + meta + ", rows=" + rows + "]";
	}

}
