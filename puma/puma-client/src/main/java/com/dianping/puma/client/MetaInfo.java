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
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Comment of MetaInfo
 * 
 * @author Leo Liang
 * 
 */
public class MetaInfo implements Serializable {

	private static final long	serialVersionUID	= 5436657168659452692L;

	private String				database;
	private String				table;
	private List<String>		columns				= new ArrayList<String>();
	private List<String>		keys				= new ArrayList<String>();

	/**
	 * @param database
	 * @param table
	 * @param columns
	 * @param keys
	 */
	public MetaInfo(String database, String table, List<String> columns, List<String> keys) {
		super();
		this.database = database;
		this.table = table;
		this.columns = columns;
		this.keys = keys;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @return the columns
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * @return the keys
	 */
	public List<String> getKeys() {
		return keys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MetaInfo [database=" + database + ", table=" + table + ", columns=" + columns + ", keys=" + keys + "]";
	}

}
