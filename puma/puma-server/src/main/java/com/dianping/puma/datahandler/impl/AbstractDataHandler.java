/**
 * Project: ${puma-datahandler.aid}
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
package com.dianping.puma.datahandler.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.common.mysql.event.QueryEvent;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.datahandler.DataHandlerResult;
import com.dianping.puma.datahandler.TableMetaInfo;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * TODO Comment of AbstractDataHandler
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public abstract class AbstractDataHandler implements DataHandler {
	private static final Logger									log					= Logger.getLogger(AbstractDataHandler.class);
	private static AtomicReference<Map<String, TableMetaInfo>>	tableMetaInfoCache	= new AtomicReference<Map<String, TableMetaInfo>>();
	private int													metaDBPort			= 3306;
	private String												metaDBHost;
	private String												metaDBUser;
	private String												metaDBPassword;
	private MysqlDataSource										metaDs;

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
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {
		refreshTableMeta();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {

	}

	protected void refreshTableMeta() {
		Map<String, TableMetaInfo> newTableMeta = new HashMap<String, TableMetaInfo>();

		if (metaDs == null) {
			metaDs = new MysqlDataSource();
			metaDs.setUrl("jdbc:mysql://" + metaDBHost + ":" + metaDBPort);
			metaDs.setUser(metaDBUser);
			metaDs.setPassword(metaDBPassword);
		}

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = metaDs.getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, COLUMN_KEY FROM INFORMATION_SCHEMA.COLUMNS");
			if (rs != null) {
				while (rs.next()) {
					String db = rs.getString("TABLE_SCHEMA");
					String tb = rs.getString("TABLE_NAME");
					String columnName = rs.getString("COLUMN_NAME");
					int colPosition = rs.getInt("ORDINAL_POSITION");
					String type = rs.getString("DATA_TYPE");
					String key = rs.getString("COLUMN_KEY");
					TableMetaInfo tmi = newTableMeta.get(db + "." + tb);
					if (tmi == null) {
						TableMetaInfo newTmi = new TableMetaInfo();
						newTmi.setDatabase(db);
						newTmi.setTable(tb);
						newTmi.setColumns(new HashMap<Integer, String>());
						newTmi.setKeys(new ArrayList<String>());
						newTmi.setTypes(new HashMap<String, String>());
						newTableMeta.put(db + "." + tb, newTmi);
						tmi = newTmi;
					}
					tmi.getColumns().put(colPosition, columnName);
					tmi.getTypes().put(columnName, convertTypes(type));
					if ("PRI".equals(key)) {
						tmi.getKeys().add(columnName);
					}
				}
				tableMetaInfoCache.set(newTableMeta);
			}
		} catch (Exception e) {
			log.error("Refresh TableMeta failed.", e);
		} finally {
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

	private String convertTypes(String str) {
		return str;
	}

	@Override
	public DataHandlerResult process(BinlogEvent binlogEvent, PumaContext context) {
		DataHandlerResult result = new DataHandlerResult();
		if (binlogEvent instanceof PumaIgnoreEvent) {
			log.info("Ingore one unknown event. eventType: " + binlogEvent.getHeader().getEventType());
			result.setEmpty(true);
			result.setFinished(true);
			return result;
		}

		byte eventType = binlogEvent.getHeader().getEventType();

		if (eventType == BinlogConstanst.STOP_EVENT || eventType == BinlogConstanst.ROTATE_EVENT) {
			result.setEmpty(true);
			result.setFinished(true);
		} else if (eventType == BinlogConstanst.QUERY_EVENT) {
			QueryEvent queryEvent = (QueryEvent) binlogEvent;
			String sql = StringUtils.trim(queryEvent.getSql());
			if (StringUtils.startsWithIgnoreCase(sql, "ALTER ") || StringUtils.startsWithIgnoreCase(sql, "CREATE ")
					|| StringUtils.startsWithIgnoreCase(sql, "DROP ")
					|| StringUtils.startsWithIgnoreCase(sql, "RENAME ")
					|| StringUtils.startsWithIgnoreCase(sql, "TRUNCATE ")) {
				// Refresh table meta
				refreshTableMeta();

				ChangedEvent dataChangedEvent = new DdlEvent();
				DdlEvent ddlEvent = (DdlEvent) dataChangedEvent;
				ddlEvent.setSql(sql);
				ddlEvent.setDatabase(queryEvent.getDatabaseName());
				ddlEvent.setExecuteTime(queryEvent.getHeader().getTimestamp());

				result.setData(dataChangedEvent);
				result.setEmpty(false);
				result.setFinished(true);

			} else if (StringUtils.equalsIgnoreCase(sql, "BEGIN")) {
				// BEGIN事件，发送一个begin transaction的事件
				ChangedEvent dataChangedEvent = new RowChangedEvent();
				((RowChangedEvent) dataChangedEvent).setTransactionBegin(true);

				result.setData(dataChangedEvent);
				result.setEmpty(false);
				result.setFinished(true);
			} else {
				result.setEmpty(true);
				result.setFinished(true);
			}
		} else {
			if (eventType == BinlogConstanst.XID_EVENT) {
				// commit事件，发送一个commit transaction的事件
				ChangedEvent dataChangedEvent = new RowChangedEvent();
				((RowChangedEvent) dataChangedEvent).setTransactionCommit(true);

				result.setData(dataChangedEvent);
				result.setEmpty(false);
				result.setFinished(true);
			} else {
				doProcess(result, binlogEvent, context, eventType);
			}
		}

		return result;
	}

	protected abstract void doProcess(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context,
			byte eventType);

	protected TableMetaInfo getTableMetaInfo(String database, String table) {
		return tableMetaInfoCache.get().get(database + "." + table);
	}

}