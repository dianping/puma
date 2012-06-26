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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.client.TableMetaInfo;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.common.mysql.event.QueryEvent;
import com.dianping.puma.datahandler.DataHandler;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * TODO Comment of AbstractDataHandler
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractDataHandler implements DataHandler {
	private static final Logger									log					= Logger.getLogger(AbstractDataHandler.class);
	private static AtomicReference<Map<String, TableMetaInfo>>	tableMetaInfoCache	= new AtomicReference<Map<String, TableMetaInfo>>();
	private int													port				= 3306;
	private String												host;
	private String												user;
	private String												password;

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
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

		MysqlDataSource ds = new MysqlDataSource();
		ds.setUrl("jdbc:mysql://" + host + ":" + port);
		ds.setUser(user);
		ds.setPassword(password);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
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
						newTmi.setTypes(new HashMap<String, Integer>());
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

	private int convertTypes(String str) {
		if (str == null) {
			return Types.OTHER;
		} else {
			int endPos = str.indexOf("(");
			if (endPos != -1) {
				str = str.substring(0, endPos);
			}
			if ("TINYINT".equalsIgnoreCase(str)) {
				return Types.TINYINT;
			} else if ("SMALLINT".equalsIgnoreCase(str)) {
				return Types.SMALLINT;
			} else if ("MEDIUMINT".equalsIgnoreCase(str)) {
				return Types.INTEGER;
			} else if ("BIGINT".equalsIgnoreCase(str)) {
				return Types.BIGINT;
			} else if ("FLOAT".equalsIgnoreCase(str)) {
				return Types.FLOAT;
			} else if ("DOUBLE".equalsIgnoreCase(str)) {
				return Types.DOUBLE;
			} else if ("DECIMAL".equalsIgnoreCase(str)) {
				return Types.DECIMAL;
			} else if ("BIT".equalsIgnoreCase(str)) {
				return Types.BIT;
			} else if ("CHAR".equalsIgnoreCase(str)) {
				return Types.CHAR;
			} else if ("VARCHAR".equalsIgnoreCase(str)) {
				return Types.VARCHAR;
			} else if ("TINYTEXT".equalsIgnoreCase(str)) {
				return Types.VARCHAR;
			} else if ("TEXT".equalsIgnoreCase(str)) {
				return Types.LONGVARCHAR;
			} else if ("MEDIUMTEXT".equalsIgnoreCase(str)) {
				return Types.LONGVARCHAR;
			} else if ("LONGTEXT".equalsIgnoreCase(str)) {
				return Types.LONGVARCHAR;
			} else if ("BINARY".equalsIgnoreCase(str)) {
				return Types.BINARY;
			} else if ("TINYBLOB".equalsIgnoreCase(str)) {
				return Types.BLOB;
			} else if ("ENUM".equalsIgnoreCase(str)) {
				return Types.INTEGER;
			} else if ("SET".equalsIgnoreCase(str)) {
				return Types.BIGINT;
			} else if ("DATE".equalsIgnoreCase(str)) {
				return Types.DATE;
			} else if ("DATETIME".equalsIgnoreCase(str)) {
				return Types.TIME;
			} else if ("TIME".equalsIgnoreCase(str)) {
				return Types.TIME;
			} else if ("TIMESTAMP".equalsIgnoreCase(str)) {
				return Types.TIMESTAMP;
			} else if ("YEAR".equalsIgnoreCase(str)) {
				return Types.INTEGER;
			} else {
				return Types.OTHER;
			}
		}
	}

	@Override
	public DataChangedEvent process(BinlogEvent binlogEvent, PumaContext context) {
		if (binlogEvent instanceof PumaIgnoreEvent) {
			log.info("Ingore one unknown event. eventType: " + binlogEvent.getHeader().getEventType());
		}
		if (binlogEvent instanceof QueryEvent) {
			QueryEvent queryEvent = (QueryEvent) binlogEvent;
			String sql = queryEvent.getSql();
			if (StringUtils.startsWithIgnoreCase(StringUtils.trim(sql), "ALTER ")) {
				refreshTableMeta();
			}
		}

		byte eventType = binlogEvent.getHeader().getEventType();

		if (eventType == BinlogConstanst.STOP_EVENT || eventType == BinlogConstanst.ROTATE_EVENT) {
			return null;
		} else {
			return doProcess(binlogEvent, context, eventType);
		}
	}

	protected abstract DataChangedEvent doProcess(BinlogEvent binlogEvent, PumaContext context, byte eventType);

	protected TableMetaInfo getTableMetaInfo(String database, String table) {
		return tableMetaInfoCache.get().get(database + "." + table);
	}

}
