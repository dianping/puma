/**
 * Project: puma-server
 * 
 * File Created at 2012-8-3
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * TODO Comment of DefaultTableMetaInfoFetcher
 * 
 * @author Leo Liang
 * 
 */
public class DefaultTableMetaInfoFetcher implements TableMetasInfoFetcher {
	private static final Logger log = Logger.getLogger(DefaultTableMetaInfoFetcher.class);
	private AtomicReference<Map<String, TableMetaInfo>> tableMetaInfoCache = new AtomicReference<Map<String, TableMetaInfo>>();
	private int metaDBPort = 3306;
	private String metaDBHost;
	private String metaDBUser;
	private String metaDBPassword;
	private MysqlDataSource metaDs;

	public int getMetaDBPort() {
		return metaDBPort;
	}

	public void setMetaDBPort(int metaDBPort) {
		this.metaDBPort = metaDBPort;
	}

	public String getMetaDBHost() {
		return metaDBHost;
	}

	public void setMetaDBHost(String metaDBHost) {
		this.metaDBHost = metaDBHost;
	}

	public String getMetaDBUser() {
		return metaDBUser;
	}

	public void setMetaDBUser(String metaDBUser) {
		this.metaDBUser = metaDBUser;
	}

	public String getMetaDBPassword() {
		return metaDBPassword;
	}

	public void setMetaDBPassword(String metaDBPassword) {
		this.metaDBPassword = metaDBPassword;
	}

	public MysqlDataSource getMetaDs() {
		return metaDs;
	}

	public void setMetaDs(MysqlDataSource metaDs) {
		this.metaDs = metaDs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.datahandler.TableMetasInfoFetcher#refreshTableMeta()
	 */
	@Override
	public void refreshTableMeta() {

		initDsIfNeeded();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = metaDs.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, COLUMN_KEY, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS");
			if (rs != null) {
				fillTableMetaCache(rs);
			}
		} catch (Exception e) {
			log.error("Refresh TableMeta failed.", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * @param newTableMeta
	 * @param rs
	 * @throws SQLException
	 */
	protected void fillTableMetaCache(ResultSet rs) throws SQLException {
		Map<String, TableMetaInfo> newTableMeta = new HashMap<String, TableMetaInfo>();
		while (rs.next()) {
			String db = rs.getString("TABLE_SCHEMA");
			String tb = rs.getString("TABLE_NAME");
			String columnName = rs.getString("COLUMN_NAME");
			int colPosition = rs.getInt("ORDINAL_POSITION");
			String type = rs.getString("DATA_TYPE");
			String key = rs.getString("COLUMN_KEY");
			String typeStr = rs.getString("COLUMN_TYPE");
			boolean signed = true;
			if (typeStr != null && typeStr.indexOf(" unsigned") != -1) {
				signed = false;
			}
			TableMetaInfo tmi = newTableMeta.get(db + "." + tb);
			if (tmi == null) {
				TableMetaInfo newTmi = new TableMetaInfo();
				newTmi.setDatabase(db);
				newTmi.setTable(tb);
				newTmi.setColumns(new HashMap<Integer, String>());
				newTmi.setKeys(new ArrayList<String>());
				newTmi.setTypes(new HashMap<String, String>());
				newTmi.setSignedInfos(new HashMap<Integer, Boolean>());
				newTableMeta.put(db + "." + tb, newTmi);
				tmi = newTmi;
			}
			tmi.getColumns().put(colPosition, columnName);
			tmi.getSignedInfos().put(colPosition, signed);
			tmi.getTypes().put(columnName, convertTypes(type));
			if ("PRI".equals(key)) {
				tmi.getKeys().add(columnName);
			}
		}
		tableMetaInfoCache.set(newTableMeta);
		if(log.isDebugEnabled()) {
			log.debug("tables meta info:" + newTableMeta);
		}
	}

	/**
	 * 
	 */
	protected void initDsIfNeeded() {
		if (metaDs == null) {
			metaDs = new MysqlDataSource();
			metaDs.setUrl("jdbc:mysql://" + metaDBHost + ":" + metaDBPort);
			metaDs.setUser(metaDBUser);
			metaDs.setPassword(metaDBPassword);
		}
	}

	protected String convertTypes(String str) {
		return str;
	}

	@Override
	public TableMetaInfo getTableMetaInfo(String database, String table) {
		return tableMetaInfoCache.get().get(database + "." + table);
	}
}
