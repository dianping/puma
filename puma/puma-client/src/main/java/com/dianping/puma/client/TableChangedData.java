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
 * TODO Comment of TableChangedData
 * 
 * @author Leo Liang
 * 
 */
public class TableChangedData implements Serializable {

	private static final long	serialVersionUID	= -2383357060735671907L;

	private MetaInfo			meta;
	List<RowChangedData>		rows;

	/**
	 * @param meta
	 * @param rows
	 */
	public TableChangedData(MetaInfo meta, List<RowChangedData> rows) {
		super();
		this.meta = meta;
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
	public MetaInfo getMeta() {
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
