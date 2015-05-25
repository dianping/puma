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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.AcceptedTables;
import com.dianping.puma.filter.TableMetaRefreshFilter;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * TODO Comment of DefaultTableMetaInfoFetcher
 *
 * @author Leo Liang
 */
public class DefaultTableMetaInfoFetcher implements TableMetaInfoFetcher {
	
	private static final Logger log = Logger.getLogger(DefaultTableMetaInfoFetcher.class);

	private AtomicReference<Map<String, TableMetaInfo>> tableMetaInfoCache = new AtomicReference<Map<String, TableMetaInfo>>();

	private int metaDBPort = 3306;

	private String metaDBHost;

	private String metaDBUsername;

	private String metaDBPassword;

	private MysqlDataSource metaDs;

	// private Map<String, AcceptedTables> acceptedDataTables;

	private TableMetaRefreshFilter tableMetaRefreshFilter;

	private static final String QUERY_SQL = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, COLUMN_KEY, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS ";

	private static final String WHERE_SQL = "WHERE ";

	private static final String TABLE_SCHEMA = "LOWER(TABLE_SCHEMA) ";

	private static final String TABLE_NAME = "LOWER(TABLE_NAME) ";

	private static final String IN_SQL = "IN ";

	private static final String AND_SQL = " AND ";

	private static final String OR_SQL = " OR ";

	private static final String EQUAL_SQL = " = ";

	private static final String PREFIX_BRACKET = " ( ";

	private static final String SUFIX_BRACKET = " ) ";

	private static final String INFIX_REPLACE = "?";

	private static final String INFIX_DOT = ",";

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

	public String getMetaDBUsername() {
		return metaDBUsername;
	}

	public void setMetaDBUsername(String metaDBUsername) {
		this.metaDBUsername = metaDBUsername;
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
	public void refreshTableMeta(DdlEvent ddlEvent, boolean isRefresh) {
		if (!isRefresh && !tableMetaRefreshFilter.accept(ddlEvent)) {
			return;
		}
		log.info("table meta refresh. ");
		initDsIfNeeded();

		Connection conn = null;
		PreparedStatement ps = null;
		// Statement stmt = null;
		ResultSet rs = null;
		Map<String, List<String>> acceptedDataTables = tableMetaRefreshFilter.getAcceptedTables().mapSchemaTables();
		Transaction t = Cat.newTransaction("SQL.meta", "information_schema.columns");

		try {
			conn = metaDs.getConnection();
			String sql = getSqlQuery(acceptedDataTables);
			log.info("table meta refresh SQL: " + sql);
			ps = (PreparedStatement) conn.prepareStatement(sql);
			setStatementParams(ps, acceptedDataTables);

			rs = ps.executeQuery();
			if (rs != null) {
				fillTableMetaCache(rs);
			}

			t.setStatus("0");
		} catch (Exception e) {
			t.setStatus(e);
			log.error("Refresh TableMeta failed.", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}

			t.complete();
		}
	}

	private String getSqlQuery(Map<String, List<String>> acceptedDataTables) {
		StringBuilder sqlStr = new StringBuilder();
		sqlStr.append(QUERY_SQL);
		if (acceptedDataTables == null || acceptedDataTables.isEmpty()) {
			return QUERY_SQL;
		}
		sqlStr.append(WHERE_SQL);
		for (Map.Entry<String, List<String>> database : acceptedDataTables.entrySet()) {
			if (StringUtils.isNotBlank(database.getKey().trim())) {
				sqlStr.append(PREFIX_BRACKET + TABLE_SCHEMA + EQUAL_SQL + INFIX_REPLACE);
				if (database.getValue() != null && database.getValue().size() > 0) {
					sqlStr.append(AND_SQL + TABLE_NAME + IN_SQL + PREFIX_BRACKET);
					for (String table : database.getValue()) {
						sqlStr.append(INFIX_REPLACE + INFIX_DOT);
					}
					sqlStr = sqlStr.delete(sqlStr.length() - INFIX_DOT.length(), sqlStr.length());
					sqlStr.append(SUFIX_BRACKET);
				}
				sqlStr.append(SUFIX_BRACKET + OR_SQL);
			}
		}
		sqlStr = sqlStr.delete(sqlStr.length() - OR_SQL.length(), sqlStr.length());
		return sqlStr.toString();
	}

	private void setStatementParams(PreparedStatement ps, Map<String, List<String>> acceptedDataTables)
			throws SQLException {
		if (acceptedDataTables == null || acceptedDataTables.isEmpty()) {
			return;
		}
		int signal = 0;
		for (Map.Entry<String, List<String>> database : acceptedDataTables.entrySet()) {
			if (StringUtils.isNotBlank(database.getKey().trim())) {
				ps.setString(++signal, database.getKey().trim());
				if (database.getValue() != null && database.getValue().size() > 0) {
					for (String table : database.getValue()) {
						ps.setString(++signal, table);
					}
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
				log.info("table meta info :" + db + "." + tb);
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
		if (log.isDebugEnabled()) {
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
			metaDs.setUser(metaDBUsername);
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

	public TableMetaRefreshFilter getTableMetaRefreshFilter() {
		return tableMetaRefreshFilter;
	}

	public void setTableMetaRefreshFilter(TableMetaRefreshFilter tableMetaRefreshFilter) {
		this.tableMetaRefreshFilter = tableMetaRefreshFilter;
	}

}
