package com.dianping.puma.syncserver.mysql;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.event.DdlEvent;

public class DdlSqlParser {

	private static final String PREFIX_TB = " TABLE ";
	private static final String INFIX_DB_TB_SPOT = ".";
	private static final String INFIX_DB_TB_QUOTE = "`";
	private static final String INFIX_SINGLE_BLANK = " ";
	private static final String PREFIX_DB = " DATABASE ";
	private static final String PREFIX_EXISTS = "IF EXISTS ";
	private static final String PREFIX_NOT_EXISTS = "IF NOT EXISTS ";
	private static final String ALTER_TANLE_SQL = "ALTER TABLE ";

	private static final String INFIX_RENAME = " RENAME ";

	public static String getAlterTableSql(DdlEvent event, String dbMappingName, String tblMappingName) {
		String strSql = StringUtils.normalizeSpace(event.getSql());
		if (StringUtils.isBlank(dbMappingName) || StringUtils.isBlank(tblMappingName)) {
			return null;
		}
		if (StringUtils.containsIgnoreCase(strSql, INFIX_RENAME)) {
			// 停止任務
			//throw new DdlRenameException("Rename error : ddl sql = " + strSql);
		}
		int tblPosition = StringUtils.indexOfIgnoreCase(strSql, PREFIX_TB) + PREFIX_TB.length();
		if (tblPosition >= PREFIX_TB.length()) {
			String subSql = StringUtils.substring(strSql, tblPosition, strSql.length());
			if (StringUtils.startsWithIgnoreCase(subSql, PREFIX_EXISTS)) {
				subSql = StringUtils.substring(subSql, PREFIX_EXISTS.length());
			} else if (StringUtils.startsWithIgnoreCase(subSql, PREFIX_NOT_EXISTS)) {
				subSql = StringUtils.substring(subSql, PREFIX_NOT_EXISTS.length());
			}
			String remainSql = StringUtils.substringAfter(subSql, INFIX_SINGLE_BLANK);
			remainSql = StringUtils.replace(StringUtils.replace(remainSql, INFIX_SINGLE_BLANK + INFIX_DB_TB_QUOTE
					+ event.getTable() + INFIX_DB_TB_QUOTE + INFIX_DB_TB_SPOT, INFIX_SINGLE_BLANK + INFIX_DB_TB_QUOTE
					+ tblMappingName + INFIX_DB_TB_QUOTE + INFIX_DB_TB_SPOT), INFIX_SINGLE_BLANK + event.getTable()
					+ INFIX_DB_TB_SPOT, INFIX_SINGLE_BLANK + tblMappingName + INFIX_DB_TB_SPOT);
			return ALTER_TANLE_SQL + dbMappingName + INFIX_DB_TB_SPOT + tblMappingName + INFIX_SINGLE_BLANK + remainSql;
		}
		return null;
	}

	public static String getRenameTableSql(DdlEvent event, String dbMappingName, String tblMappingName) {
		if (!StringUtils.isBlank(dbMappingName) && !StringUtils.isBlank(tblMappingName)) {
			//throw new Exception("Rename error : ddl sql = " + event.getSql());
		}
		return null;
	}
	
}
