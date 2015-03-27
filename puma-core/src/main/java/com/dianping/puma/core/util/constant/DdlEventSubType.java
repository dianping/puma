package com.dianping.puma.core.util.constant;

import org.apache.commons.lang.StringUtils;

public enum DdlEventSubType {
	/* alter event */
	DDL_ALTER_DATABASE, DDL_ALTER_EVENT, DDL_ALTER_FUNCTION, DDL_ALTER_PROCEDURE, DDL_ALTER_SERVER, DDL_ALTER_TABLE, DDL_ALTER_TABLESPACE,
	/* create event */
	DDL_CREATE_DATABASE, DDL_CREATE_EVENT, DDL_CREATE_FUNCTION, DDL_CREATE_INDEX, DDL_CREATE_PROCEDURE, DDL_CREATE_SERVER, DDL_CREATE_TABLE, DDL_CREATE_TRIGGER, DDL_CREATE_USER, DDL_CREATE_VIEW,
	/* drop event */
	DDL_DROP_DATABASE, DDL_DROP_EVENT, DDL_DROP_FUNCTION, DDL_DROP_INDEX, DDL_DROP_PROCEDURE, DDL_DROP_SERVER, DDL_DROP_TABLE, DDL_DROP_TRIGGER, DDL_DROP_USER, DDL_DROP_VIEW,
	/* rename event */
	DDL_RENAME_TABLE, DDL_RENAME_USER,
	/* truncate event */
	DDL_TRUNCATE_TABLE,
	/* default */
	DDL_SUB_DEFAULT;

	private static final String STR_SUB_DATABASE = "DATABASE";
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

	public static DdlEventSubType getEventSubType(DdlEventType eventType, String strPrefix) {
		if(StringUtils.isBlank(strPrefix)||eventType==DdlEventType.DDL_DEFAULT){
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
				eventSubType =  DdlEventSubType.DDL_SUB_DEFAULT;
				break;
		}
		return eventSubType;
	}

	private static DdlEventSubType getAlterEventType(String strPrefix) {
		if (strPrefix.equalsIgnoreCase(STR_SUB_DATABASE)) {
			return DdlEventSubType.DDL_ALTER_DATABASE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_EVENT)) {
			return DdlEventSubType.DDL_ALTER_EVENT;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_FUNCTION)) {
			return DdlEventSubType.DDL_ALTER_FUNCTION;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_PROCEDURE)) {
			return DdlEventSubType.DDL_ALTER_PROCEDURE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_SERVER)) {
			return DdlEventSubType.DDL_ALTER_SERVER;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_ALTER_TABLE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_TABLESPACE)) {
			return DdlEventSubType.DDL_ALTER_TABLESPACE;
		} else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}
	
	private static DdlEventSubType getCreateEventType(String strPrefix) {
		if (strPrefix.equalsIgnoreCase(STR_SUB_DATABASE)) {
			return DdlEventSubType.DDL_CREATE_DATABASE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_EVENT)) {
			return DdlEventSubType.DDL_CREATE_EVENT;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_FUNCTION)) {
			return DdlEventSubType.DDL_CREATE_FUNCTION;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_INDEX)) {
			return DdlEventSubType.DDL_CREATE_INDEX;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_PROCEDURE)) {
			return DdlEventSubType.DDL_CREATE_PROCEDURE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_SERVER)) {
			return DdlEventSubType.DDL_CREATE_SERVER;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_CREATE_TABLE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_TRIGGER)) {
			return DdlEventSubType.DDL_CREATE_TRIGGER;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_USER)) {
			return DdlEventSubType.DDL_CREATE_USER;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_VIEW)) {
			return DdlEventSubType.DDL_CREATE_VIEW;
		}else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}
	
	private static DdlEventSubType getDropEventType(String strPrefix) {
		if (strPrefix.equalsIgnoreCase(STR_SUB_DATABASE)) {
			return DdlEventSubType.DDL_DROP_DATABASE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_EVENT)) {
			return DdlEventSubType.DDL_DROP_EVENT;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_FUNCTION)) {
			return DdlEventSubType.DDL_DROP_FUNCTION;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_INDEX)) {
			return DdlEventSubType.DDL_DROP_INDEX;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_PROCEDURE)) {
			return DdlEventSubType.DDL_DROP_PROCEDURE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_SERVER)) {
			return DdlEventSubType.DDL_DROP_SERVER;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_DROP_TABLE;
		} else if (strPrefix.equalsIgnoreCase(STR_SUB_TRIGGER)) {
			return DdlEventSubType.DDL_DROP_TRIGGER;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_USER)) {
			return DdlEventSubType.DDL_DROP_USER;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_VIEW)) {
			return DdlEventSubType.DDL_DROP_VIEW;
		}else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}

	private static DdlEventSubType getRenameEventType(String strPrefix) {
		if (strPrefix.equalsIgnoreCase(STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_RENAME_TABLE;
		}else if (strPrefix.equalsIgnoreCase(STR_SUB_USER)) {
			return DdlEventSubType.DDL_RENAME_USER;
		}else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}
	
	private static DdlEventSubType getTruncateEventType(String strPrefix) {
		if (strPrefix.equalsIgnoreCase(STR_SUB_TABLE)) {
			return DdlEventSubType.DDL_TRUNCATE_TABLE;
		}else {
			return DdlEventSubType.DDL_SUB_DEFAULT;
		}
	}
}
