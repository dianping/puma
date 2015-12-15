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
package com.dianping.puma.parser.meta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 数据表的Meta信息
 * <tt>columns</tt>是以column index为key, 以column name为value的map
 * <tt>signedInfos</tt>是以column index为key, 以是否为有符号数为value的map
 * <tt>keys</tt>包含所有主键的column name
 * <tt>types</tt>是以column name为key，以java.sql.Types里面的类型定义为value的map
 * <tt>rawTypesCodes</tt>是以column index为key，以类型数值为value的map(元数据)
 * <tt>rawNullAbilities</tt>是所有允许为null的column index(元数据)
 * 
 * <strong>注意：<tt>columns</tt>包含所有的列名，包括主键。<tt>keys</tt>仅包含主键列名。</strong>
 * 
 * </pre>
 * 
 * @author Leo Liang
 * 
 */
public class TableMetaInfo implements Serializable {

	private static final long		serialVersionUID	= 5436657168659452692L;

	private String					database;
	private String					table;
	private Map<Integer, String>	columns;
	private Map<Integer, Boolean>	signedInfos;
	private Map<Integer, Byte>		rawTypeCodes;
	private List<Integer>			rawNullAbilities;
	private List<String>			keys;
	private Map<String, String>		types;

	/**
	 * @return the signedInfos
	 */
	public Map<Integer, Boolean> getSignedInfos() {
		return signedInfos;
	}

	/**
	 * @param signedInfos
	 *            the signedInfos to set
	 */
	public void setSignedInfos(Map<Integer, Boolean> signedInfos) {
		this.signedInfos = signedInfos;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @param table
	 *            the table to set
	 */
	public void setTable(String table) {
		this.table = table;
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
	public Map<Integer, String> getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(Map<Integer, String> columns) {
		this.columns = columns;
	}

	/**
	 * @return the keys
	 */
	public List<String> getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	/**
	 * @return the types
	 */
	public Map<String, String> getTypes() {
		return types;
	}

	/**
	 * @param types
	 *            the types to set
	 */
	public void setTypes(Map<String, String> types) {
		this.types = types;
	}

	/**
	 * @return the rawTypeCodes
	 */
	public Map<Integer, Byte> getRawTypeCodes() {
		return rawTypeCodes;
	}

	/**
	 * @param rawTypeCodes
	 *            the rawTypeCodes to set
	 */
	public void setRawTypeCodes(Map<Integer, Byte> rawTypeCodes) {
		this.rawTypeCodes = rawTypeCodes;
	}

	/**
	 * @return the rawNullAbilities
	 */
	public List<Integer> getRawNullAbilities() {
		return rawNullAbilities;
	}

	/**
	 * @param rawNullAbilities
	 *            the rawNullAbilities to set
	 */
	public void setRawNullAbilities(List<Integer> rawNullAbilities) {
		this.rawNullAbilities = rawNullAbilities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TableMetaInfo [database=" + database + ", table=" + table + ", columns=" + columns + ", signedInfos="
				+ signedInfos + ", rawTypeCodes=" + rawTypeCodes + ", rawNullAbilities=" + rawNullAbilities + ", keys="
				+ keys + ", types=" + types + "]";
	}

}
