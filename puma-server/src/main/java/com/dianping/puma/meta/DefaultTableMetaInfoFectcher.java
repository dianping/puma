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
package com.dianping.puma.meta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.puma.biz.entity.SrcDBInstance;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.datahandler.TableMetaInfo;
import com.dianping.puma.filter.TableMetaRefreshFilter;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * @author Leo Liang
 * @author hao.zhu
 */
public class DefaultTableMetaInfoFectcher implements TableMetaInfoFectcher {

	private static final Logger log = Logger.getLogger(DefaultTableMetaInfoFectcher.class);

	private SrcDBInstance srcDbInstance;

	private BinlogInfo binlogInfo;

	private MysqlDataSource metaDs;

	private TableMetaRefreshFilter tableMetaRefreshFilter;

	private TableMetaInfoStore tableMetaInfoStore;

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

	public TableMetaInfoStore getTableMetaInfoStore() {
		return tableMetaInfoStore;
	}

	public void setTableMetaInfoStore(TableMetaInfoStore tableMetaInfoStore) {
		this.tableMetaInfoStore = tableMetaInfoStore;
	}

	public SrcDBInstance getSrcDbInstance() {
		return srcDbInstance;
	}

	public void setSrcDbInstance(SrcDBInstance srcDbInstance) {
		this.srcDbInstance = srcDbInstance;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public MysqlDataSource getMetaDs() {
		return metaDs;
	}

	public void setMetaDs(MysqlDataSource metaDs) {
		this.metaDs = metaDs;
	}

	@Override
	public TableMetaInfo getTableMetaInfo(String database, String table) {
		return tableMetaInfoStore.getMostRecentMetaInfo(database, table);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.datahandler.TableMetasInfoFetcher#refreshTableMeta()
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
		ResultSet rs = null;
		Map<String, List<String>> acceptedDataTables = tableMetaRefreshFilter.getAcceptedTables().mapSchemaTables();
		Transaction t = Cat.newTransaction("SQL.Meta", "information_schema.columns");

		try {
			conn = metaDs.getConnection();
			String sql = getSqlQuery(acceptedDataTables);
			log.info("table meta refresh SQL: " + sql);
			ps = conn.prepareStatement(sql);
			setStatementParams(ps, acceptedDataTables);

			rs = ps.executeQuery();
			if (rs != null) {
				fillTableMetaCache(rs, ddlEvent);
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			log.error("Refresh TableMeta failed.", e);
			t.setStatus(e);
			Cat.logError(e);
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
		if (acceptedDataTables == null || acceptedDataTables.isEmpty()) {
			return QUERY_SQL;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(QUERY_SQL);
		sb.append(WHERE_SQL);

		for (Map.Entry<String, List<String>> database : acceptedDataTables.entrySet()) {
			if (StringUtils.isNotBlank(database.getKey().trim())) {
				sb.append(PREFIX_BRACKET + TABLE_SCHEMA + EQUAL_SQL + INFIX_REPLACE);
				if (database.getValue() != null && database.getValue().size() > 0) {
					sb.append(AND_SQL + TABLE_NAME + IN_SQL + PREFIX_BRACKET);
					List<String> value = database.getValue();
					for (int i = 0; i < value.size(); i++) {
						sb.append(INFIX_REPLACE + INFIX_DOT);
					}
					sb = sb.delete(sb.length() - INFIX_DOT.length(), sb.length());
					sb.append(SUFIX_BRACKET);
				}
				sb.append(SUFIX_BRACKET + OR_SQL);
			}
		}
		sb = sb.delete(sb.length() - OR_SQL.length(), sb.length());

		return sb.toString();
	}

	private void setStatementParams(PreparedStatement ps, Map<String, List<String>> acceptedDataTables)
	      throws SQLException {
		if (acceptedDataTables == null || acceptedDataTables.isEmpty()) {
			return;
		}
		int signal = 0;
		for (Map.Entry<String, List<String>> database : acceptedDataTables.entrySet()) {
			String key = database.getKey().trim();
			if (StringUtils.isNotBlank(key)) {
				ps.setString(++signal, key);
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
	 * @throws IOException
	 */
	protected void fillTableMetaCache(ResultSet rs, DdlEvent ddlEvent) throws SQLException, IOException {
		Map<String, TableMetaInfo> ddl = new HashMap<String, TableMetaInfo>();

		while (rs.next()) {
			String db = rs.getString("TABLE_SCHEMA");
			if(db.equals("information_schema") || db.equals("mysql") || db.equals("performance_schema")){
				continue;
			}
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

			TableMetaInfo meta = ddl.get(db + "-" + tb);
			if (meta == null) {
				meta = new TableMetaInfo();
				ddl.put(db + "-" + tb, meta);
				meta.setDatabase(db);
				meta.setTable(tb);
				meta.setColumns(new HashMap<Integer, String>());
				meta.setKeys(new ArrayList<String>());
				meta.setTypes(new HashMap<String, String>());
				meta.setSignedInfos(new HashMap<Integer, Boolean>());
			}
			meta.getColumns().put(colPosition, columnName);
			meta.getSignedInfos().put(colPosition, signed);
			meta.getTypes().put(columnName, convertTypes(type));
			if ("PRI".equals(key)) {
				meta.getKeys().add(columnName);
			}
		}

		DdlEvent event;
		if (ddlEvent != null) {
			event = ddlEvent;
		} else {
			event = new DdlEvent();
			event.setBinlogInfo(binlogInfo);
			event.setBinlogServerId(srcDbInstance.getServerId());
		}

		for (Entry<String, TableMetaInfo> entry : ddl.entrySet()) {
			tableMetaInfoStore.addTableMetaInfo(event, entry.getValue());
		}
	}

	/**
	 *
	 */
	protected void initDsIfNeeded() {
		if (metaDs == null) {
			metaDs = new MysqlDataSource();
			metaDs.setUrl("jdbc:mysql://" + srcDbInstance.getHost() + ":" + srcDbInstance.getPort());
			metaDs.setUser(srcDbInstance.getUsername());
			metaDs.setPassword(srcDbInstance.getPassword());
		}
	}

	protected String convertTypes(String str) {
		return str;
	}

	public TableMetaRefreshFilter getTableMetaRefreshFilter() {
		return tableMetaRefreshFilter;
	}

	public void setTableMetaRefreshFilter(TableMetaRefreshFilter tableMetaRefreshFilter) {
		this.tableMetaRefreshFilter = tableMetaRefreshFilter;
	}

}
