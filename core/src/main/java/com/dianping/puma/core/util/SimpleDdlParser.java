package com.dianping.puma.core.util;

import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;
import org.apache.commons.lang.StringUtils;

public class SimpleDdlParser {

	private static final String PREFIX_TB = " TABLE ";

	private static final String INFIX_DB_TB_SPOT = ".";

	private static final String INFIX_DB_TB_QUOTE = "`";

	private static final String INFIX_SINGLE_BLANK = " ";

	private static final String PREFIX_DB = " DATABASE ";

	private static final String PREFIX_DB_SCHEMA = " SCHEMA ";

	private static final String PREFIX_EXISTS = "IF EXISTS ";

	private static final String PREFIX_NOT_EXISTS = "IF NOT EXISTS ";

	public static DdlEventType getEventType(String strSql) {
		strSql = StringUtils.normalizeSpace(strSql);
		String strPrefix = StringUtils.substringBefore(strSql, INFIX_SINGLE_BLANK);
		return DdlEventType.getEventType(strPrefix);
	}

	public static DdlEventSubType getEventSubType(DdlEventType eventType, String strSql) {
		strSql = StringUtils.normalizeSpace(strSql);
		String strPrefix = StringUtils.substringAfter(strSql, INFIX_SINGLE_BLANK);
		return DdlEventSubType.getEventSubType(eventType, strPrefix);
	}

	public static DdlResult getDdlResult(DdlEventType eventType, DdlEventSubType eventSubType, String strSql) {
		strSql = StringUtils.normalizeSpace(strSql);
		switch (eventType) {
		case DDL_ALTER:
			return getAlterSqlNames(eventSubType, strSql);
		case DDL_CREATE:
			return getCreateSqlNames(eventSubType, strSql);
		case DDL_DROP:
			return getDropSqlNames(eventSubType, strSql);
		case DDL_RENAME:
			return getRenameSqlNames(eventSubType, strSql);
		case DDL_TRUNCATE:
			return getTruncateSqlNames(eventSubType, strSql);
		case DDL_DEFAULT:
			break;
		}
		return null;
	}

	private static DdlResult getAlterSqlNames(DdlEventSubType eventSubType, String strSql) {
		DdlResult ddlResult = null;
		switch (eventSubType) {
		case DDL_ALTER_DATABASE:
			String dbName = parseDBName(strSql);
			if (StringUtils.isNotBlank(dbName)) {
				ddlResult = new DdlResult();
				ddlResult.setDatabase(dbName);
			}
			break;
		case DDL_ALTER_EVENT:
			break;
		case DDL_ALTER_FUNCTION:
			break;
		case DDL_ALTER_PROCEDURE:
			break;
		case DDL_ALTER_SERVER:
			break;
		case DDL_ALTER_TABLE:
			ddlResult = parseNames(strSql);
			break;
		case DDL_ALTER_TABLESPACE:
			break;
		}
		return ddlResult;
	}

	private static DdlResult getCreateSqlNames(DdlEventSubType eventSubType, String strSql) {
		DdlResult ddlResult = null;
		switch (eventSubType) {
		case DDL_CREATE_DATABASE:
			String dbName = parseDBName(strSql);
			if (StringUtils.isNotBlank(dbName)) {
				ddlResult = new DdlResult();
				ddlResult.setDatabase(dbName);
			}
			break;
		case DDL_CREATE_EVENT:
			break;
		case DDL_CREATE_FUNCTION:
			break;
		case DDL_CREATE_INDEX:
			break;
		case DDL_CREATE_PROCEDURE:
			break;
		case DDL_CREATE_SERVER:
			break;
		case DDL_CREATE_TABLE:
			ddlResult = parseNames(strSql);
			break;
		case DDL_CREATE_TRIGGER:
			break;
		case DDL_CREATE_USER:
			break;
		case DDL_CREATE_VIEW:
			break;
		}
		return ddlResult;
	}

	private static DdlResult getDropSqlNames(DdlEventSubType eventSubType, String strSql) {
		DdlResult ddlResult = null;
		switch (eventSubType) {
		case DDL_DROP_DATABASE:
			String dbName = parseDBName(strSql);
			if (StringUtils.isNotBlank(dbName)) {
				ddlResult = new DdlResult();
				ddlResult.setDatabase(dbName);
			}
			break;
		case DDL_DROP_EVENT:
			break;
		case DDL_DROP_FUNCTION:
			break;
		case DDL_DROP_INDEX:
			break;
		case DDL_DROP_PROCEDURE:
			break;
		case DDL_DROP_SERVER:
			break;
		case DDL_DROP_TABLE:
			ddlResult = parseNames(strSql);
			break;
		case DDL_DROP_TRIGGER:
			break;
		case DDL_DROP_USER:
			break;
		case DDL_DROP_VIEW:
			break;
		}
		return ddlResult;
	}

	private static DdlResult getRenameSqlNames(DdlEventSubType eventSubType, String strSql) {
		DdlResult ddlResult = null;
		switch (eventSubType) {
		case DDL_RENAME_TABLE:
			ddlResult = parseNames(strSql);
		case DDL_RENAME_USER:
			break;
		}
		return ddlResult;
	}

	private static DdlResult getTruncateSqlNames(DdlEventSubType eventSubType, String strSql) {
		DdlResult ddlResult = null;
		switch (eventSubType) {
		case DDL_TRUNCATE_TABLE:
			ddlResult = parseNames(strSql);
		}
		return ddlResult;
	}

	public static DdlResult parseNames(String strSql) {
		DdlResult ddlResult = null;
		int tblPosition = StringUtils.indexOfIgnoreCase(strSql, PREFIX_TB) + PREFIX_TB.length();
		if (tblPosition >= PREFIX_TB.length()) {
			String subSql = StringUtils.substring(strSql, tblPosition, strSql.length());
			if (StringUtils.startsWithIgnoreCase(subSql, PREFIX_EXISTS)) {
				subSql = StringUtils.substring(subSql, PREFIX_EXISTS.length());
			} else if (StringUtils.startsWithIgnoreCase(subSql, PREFIX_NOT_EXISTS)) {
				subSql = StringUtils.substring(subSql, PREFIX_NOT_EXISTS.length());
			}
			String strTbl = StringUtils.substringBefore(subSql, INFIX_SINGLE_BLANK);
			if (StringUtils.isNotBlank(strTbl)) {
				int midPosition = StringUtils.indexOf(strTbl, INFIX_DB_TB_SPOT);
				ddlResult = new DdlResult();
				if (midPosition > -1) {
					ddlResult.setDatabase(StringUtils.remove(StringUtils.substringBefore(strTbl, INFIX_DB_TB_SPOT),
					      INFIX_DB_TB_QUOTE));
					ddlResult.setTable(StringUtils.remove(StringUtils.substringAfter(strTbl, INFIX_DB_TB_SPOT),
					      INFIX_DB_TB_QUOTE));
				} else {
					ddlResult.setTable(StringUtils.remove(strTbl, INFIX_DB_TB_QUOTE));
				}
			}
		}
		return ddlResult;
	}

	public static String parseDBName(String strSql) {
		int dbPosition = StringUtils.indexOfIgnoreCase(strSql, PREFIX_DB) + PREFIX_DB.length();
		if (dbPosition < PREFIX_DB.length()) {
			dbPosition = StringUtils.indexOfIgnoreCase(strSql, PREFIX_DB_SCHEMA) + PREFIX_DB_SCHEMA.length();
		}
		String dbName = null;
		if (dbPosition >= PREFIX_DB.length()) {
			String subSql = StringUtils.substring(strSql, dbPosition, strSql.length());
			if (StringUtils.startsWithIgnoreCase(subSql, PREFIX_EXISTS)) {
				subSql = StringUtils.substring(subSql, PREFIX_EXISTS.length());
			} else if (StringUtils.startsWithIgnoreCase(subSql, PREFIX_NOT_EXISTS)) {
				subSql = StringUtils.substring(subSql, PREFIX_NOT_EXISTS.length());
			}
			dbName = StringUtils.remove(StringUtils.substringBefore(subSql, INFIX_SINGLE_BLANK), INFIX_DB_TB_QUOTE);
		}
		return dbName;
	}

	public static class DdlResult {
		private String database;

		private String table;

		public DdlResult() {

		}

		public DdlResult(String database, String table) {
			this.setDatabase(database);
			this.setTable(table);
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public String getDatabase() {
			return database;
		}

		public void setTable(String table) {
			this.table = table;
		}

		public String getTable() {
			return table;
		}

	}

}
