package com.dianping.puma.core.util.constant;

import com.dianping.puma.core.util.PatternUtils;
import org.apache.commons.lang.StringUtils;

public enum DdlEventSubType {
	/* alter event */
	DDL_ALTER_DATABASE(1), DDL_ALTER_EVENT(2), DDL_ALTER_FUNCTION(3), DDL_ALTER_PROCEDURE(4), DDL_ALTER_SERVER(5), DDL_ALTER_TABLE(
	      6), DDL_ALTER_TABLESPACE(7),
	/* create event */
	DDL_CREATE_DATABASE(8), DDL_CREATE_EVENT(9), DDL_CREATE_FUNCTION(10), DDL_CREATE_INDEX(11), DDL_CREATE_PROCEDURE(12), DDL_CREATE_SERVER(
	      13), DDL_CREATE_TABLE(14), DDL_CREATE_TRIGGER(15), DDL_CREATE_USER(16), DDL_CREATE_VIEW(17),
	/* drop event */
	DDL_DROP_DATABASE(18), DDL_DROP_EVENT(19), DDL_DROP_FUNCTION(20), DDL_DROP_INDEX(21), DDL_DROP_PROCEDURE(22), DDL_DROP_SERVER(
	      23), DDL_DROP_TABLE(24), DDL_DROP_TRIGGER(25), DDL_DROP_USER(26), DDL_DROP_VIEW(27),
	/* rename event */
	DDL_RENAME_TABLE(28), DDL_RENAME_USER(29),
	/* truncate event */
	DDL_TRUNCATE_TABLE(30),
	/* default */
	DDL_SUB_DEFAULT(31);

	private int type;

	private static final String STR_SUB_DATABASE = "DATABASE";
	
	private static final String STR_SUB_SCHEMA = "SCHEMA";
	
	private static final String STR_SUB_EVENT = "EVENT";

	private static final String STR_SUB_FUNCTION = "FUNCTION";

	private static final String STR_SUB_PROCEDURE = "PROCEDURE";

	private static final String STR_SUB_SERVER = "SERVER";

	private static final String STR_SUB_TABLE = "TABLE";

	private static final String STR_SUB_TABLESPACE = "TABLESPACE";

	private static final String STR_SUB_INDEX = "INDEX";

	private static final String STR_SUB_TRIGGER = "TRIGGER";

	private static final String STR_SUB_USER = "USER";

	private static final String STR_SUB_VIEW = "VIEW";

	private static final String STR_UNIQUE_INDEX = "UNIQUE INDEX";

	private static final String STR_FULLTEXT_INDEX = "FULLTEXT INDEX";

	private static final String STR_SPATIAL_INDEX = "SPATIAL INDEX";

	private static final String CREATE_VIEW_PATTERN = "^\\s*.*\\s*VIEW\\s*.*$";

	private static DdlEventSubType getAlterEventType(String strPrefix) {
		if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_DATABASE)
		      || StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_SCHEMA)) {
			return DdlEventSubType.DDL_ALTER_DATABASE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_EVENT)) {
			return DdlEventSubType.DDL_ALTER_EVENT;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_FUNCTION)) {
			return DdlEventSubType.DDL_ALTER_FUNCTION;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_PROCEDURE)) {
			return DdlEventSubType.DDL_ALTER_PROCEDURE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_SERVER)) {
			return DdlEventSubType.DDL_ALTER_SERVER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_ALTER_TABLE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TABLESPACE)) {
			return DdlEventSubType.DDL_ALTER_TABLESPACE;
		} else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}

	private static DdlEventSubType getCreateEventType(String strPrefix) {
		if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_DATABASE)
		      || StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_SCHEMA)) {
			return DdlEventSubType.DDL_CREATE_DATABASE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_EVENT)) {
			return DdlEventSubType.DDL_CREATE_EVENT;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_FUNCTION)) {
			return DdlEventSubType.DDL_CREATE_FUNCTION;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_INDEX)
		      || StringUtils.startsWithIgnoreCase(strPrefix, STR_UNIQUE_INDEX)
		      || StringUtils.startsWithIgnoreCase(strPrefix, STR_FULLTEXT_INDEX)
		      || StringUtils.startsWithIgnoreCase(strPrefix, STR_SPATIAL_INDEX)) {
			return DdlEventSubType.DDL_CREATE_INDEX;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_PROCEDURE)) {
			return DdlEventSubType.DDL_CREATE_PROCEDURE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_SERVER)) {
			return DdlEventSubType.DDL_CREATE_SERVER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_CREATE_TABLE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TRIGGER)) {
			return DdlEventSubType.DDL_CREATE_TRIGGER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_USER)) {
			return DdlEventSubType.DDL_CREATE_USER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_VIEW)
		      || PatternUtils.isMatches(strPrefix, CREATE_VIEW_PATTERN)) {
			return DdlEventSubType.DDL_CREATE_VIEW;
		} else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}

	public static DdlEventSubType getDdlEventSubType(int type){
		for(DdlEventSubType subType : DdlEventSubType.values()){
			if(subType.getEventSubType() == type){
				return subType;
			}
		}
		
		return DDL_SUB_DEFAULT;
	}

	private static DdlEventSubType getDropEventType(String strPrefix) {
		if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_DATABASE)
		      || StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_SCHEMA)) {
			return DdlEventSubType.DDL_DROP_DATABASE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_EVENT)) {
			return DdlEventSubType.DDL_DROP_EVENT;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_FUNCTION)) {
			return DdlEventSubType.DDL_DROP_FUNCTION;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_INDEX)) {
			return DdlEventSubType.DDL_DROP_INDEX;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_PROCEDURE)) {
			return DdlEventSubType.DDL_DROP_PROCEDURE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_SERVER)) {
			return DdlEventSubType.DDL_DROP_SERVER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_DROP_TABLE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TRIGGER)) {
			return DdlEventSubType.DDL_DROP_TRIGGER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_USER)) {
			return DdlEventSubType.DDL_DROP_USER;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_VIEW)) {
			return DdlEventSubType.DDL_DROP_VIEW;
		} else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}

	public static DdlEventSubType getEventSubType(DdlEventType eventType, String strPrefix) {
		if (StringUtils.isBlank(strPrefix) || eventType == DdlEventType.DDL_DEFAULT) {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
		DdlEventSubType eventSubType = DdlEventSubType.DDL_SUB_DEFAULT;
		switch (eventType) {
		case DDL_ALTER:
			eventSubType = getAlterEventType(strPrefix);
			break;
		case DDL_CREATE:
			eventSubType = getCreateEventType(strPrefix);
			break;
		case DDL_DROP:
			eventSubType = getDropEventType(strPrefix);
			break;
		case DDL_RENAME:
			eventSubType = getRenameEventType(strPrefix);
			break;
		case DDL_TRUNCATE:
			eventSubType = getTruncateEventType(strPrefix);
			break;
		case DDL_DEFAULT:
			eventSubType = DdlEventSubType.DDL_SUB_DEFAULT;
			break;
		}
		return eventSubType;
	}

	private static DdlEventSubType getRenameEventType(String strPrefix) {
		if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_RENAME_TABLE;
		} else if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_USER)) {
			return DdlEventSubType.DDL_RENAME_USER;
		} else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}

	private static DdlEventSubType getTruncateEventType(String strPrefix) {
		if (StringUtils.startsWithIgnoreCase(strPrefix, STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_TRUNCATE_TABLE;
		} else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}

	DdlEventSubType(int type) {
		this.type = type;
	}

	public int getEventSubType(){
		return type;
	}
}
