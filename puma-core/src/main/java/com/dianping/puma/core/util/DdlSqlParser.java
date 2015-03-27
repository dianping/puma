package com.dianping.puma.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;

public class DdlSqlParser {
	
	
	public static DdlEventType getEventType(String strSql){
		strSql = StringUtils.normalizeSpace(strSql);
		String strPrefix = StringUtils.substringBefore(strSql, " ");
		return DdlEventType.getEventType(strPrefix);
	}
	
	public static DdlEventSubType getEventSubType(DdlEventType eventType,String strSql){
		strSql = StringUtils.normalizeSpace(strSql);
		String strPrefix = StringUtils.substringBetween(strSql, " "," ");
		return DdlEventSubType.getEventSubType(eventType,strPrefix);
	}
	
	public static List<String> getSqlNames(DdlEventType eventType,DdlEventSubType eventSubType,String strSql){
		strSql = StringUtils.normalizeSpace(strSql).toLowerCase();
		switch(eventType){
			case DDL_ALTER:
				return getAlterSqlNames(eventSubType,strSql);
			case DDL_CREATE:
				return getCreateSqlNames(eventSubType,strSql);
			case DDL_DROP:
				return getDropSqlNames(eventSubType,strSql);
			case DDL_RENAME:
				return getRenameSqlNames(eventSubType,strSql);
			case DDL_TRUNCATE:
				return getTruncateSqlNames(eventSubType,strSql);
			case DDL_DEFAULT:
				break;
		}
		return null;
	}
	private static List<String> getAlterSqlNames(DdlEventSubType eventSubType,String strSql){
		List<String> sqlNames = null;
		String dbName = "";
		String tblName = "";
		switch(eventSubType){
			case DDL_ALTER_DATABASE:
				dbName = parseDBName(strSql);
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
				sqlNames = parseNames(strSql);
				break;
			case DDL_ALTER_TABLESPACE:
				break;
		}
		if(sqlNames == null){
			sqlNames = new ArrayList<String>();
			sqlNames.add(dbName);
			sqlNames.add(tblName);
		}
		return sqlNames;
	}
	
	private static List<String> getCreateSqlNames(DdlEventSubType eventSubType,String strSql){
		List<String> sqlNames = null;
		String dbName = "";
		String tblName = "";
		switch(eventSubType){
			case DDL_CREATE_DATABASE:
				dbName = parseDBName(strSql);
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
				break;
			case DDL_CREATE_TRIGGER:
				break;
			case DDL_CREATE_USER:
				break;
			case DDL_CREATE_VIEW:
				break;
		}
		if(sqlNames == null){
			sqlNames = new ArrayList<String>();
			sqlNames.add(dbName);
			sqlNames.add(tblName);
		}
		return sqlNames;
	}
	
	private static List<String> getDropSqlNames(DdlEventSubType eventSubType,String strSql){
		List<String> sqlNames = null;
		String dbName = "";
		String tblName = "";
		switch(eventSubType){
			case DDL_DROP_DATABASE:
				dbName = parseDBName(strSql);
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
				break;
			case DDL_DROP_TRIGGER:
				break;
			case DDL_DROP_USER:
				break;
			case DDL_DROP_VIEW:
				break;
		}
		if(sqlNames == null){
			sqlNames = new ArrayList<String>();
			sqlNames.add(dbName);
			sqlNames.add(tblName);
		}
		return sqlNames;
	}
	
	private static List<String> getRenameSqlNames(DdlEventSubType eventSubType,String strSql){
		List<String> sqlNames = null;
		String dbName = "";
		String tblName = "";
		switch(eventSubType){
			case DDL_RENAME_TABLE:
				sqlNames = parseNames(strSql);
			case DDL_RENAME_USER:
				break;
		}
		if(sqlNames == null){
			sqlNames = new ArrayList<String>();
			sqlNames.add(dbName);
			sqlNames.add(tblName);
		}
		return sqlNames;
	}
	
	private static List<String> getTruncateSqlNames(DdlEventSubType eventSubType,String strSql){
		List<String> sqlNames = null;
		String dbName = "";
		String tblName = "";
		switch(eventSubType){
			case DDL_TRUNCATE_TABLE:
				sqlNames =parseNames(strSql);
		}
		if(sqlNames == null){
			sqlNames = new ArrayList<String>();
			sqlNames.add(dbName);
			sqlNames.add(tblName);
		}
		return sqlNames;
	}
	
	public static List<String> parseNames(String strSql){
		List<String> sqlNames = null;
		int tblPosition = StringUtils.indexOf(strSql, " table ") + 7;
		if(tblPosition > 6){
			String subSql = StringUtils.substring(strSql, tblPosition, strSql.length());
			if(StringUtils.startsWithIgnoreCase(subSql, "if exists ")){
				subSql = StringUtils.substringAfter(subSql, "if exists ");
			}else if(StringUtils.startsWithIgnoreCase(subSql, "if no exists ")){
				subSql = StringUtils.substringAfter(subSql, "if no exists ");
			}
			String strTbl= StringUtils.substringBefore(subSql, " ");
			if(!StringUtils.isBlank(strTbl)){
				int midPosition = StringUtils.indexOf(strTbl,".");
				sqlNames = new ArrayList<String>();
				if(midPosition >-1 ){
					sqlNames.add(StringUtils.remove(StringUtils.substringBefore(strTbl, "."), "`"));
					sqlNames.add(StringUtils.remove(StringUtils.substringAfter(strTbl, "."), "`"));
				}else{
					sqlNames.add("");
					sqlNames.add(strTbl);
				}
			}
		}	
		return sqlNames;
	}
	
	
	public static String parseDBName(String strSql){
		int dbPosition = StringUtils.indexOf(strSql, " database ") + 10;
		String dbName = null;
		if(dbPosition > 9){
			String subSql = StringUtils.substring(strSql, dbPosition, strSql.length());
			if(StringUtils.startsWithIgnoreCase(subSql, "if exists ")){
				subSql = StringUtils.substringAfter(subSql, "if exists ");
			}else if(StringUtils.startsWithIgnoreCase(subSql, "if no exists ")){
				subSql = StringUtils.substringAfter(subSql, "if no exists ");
			}
			dbName = StringUtils.substringBefore(subSql, " ");
		}
		return dbName;
	}
}
