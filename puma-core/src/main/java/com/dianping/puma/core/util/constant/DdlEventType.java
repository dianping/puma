package com.dianping.puma.core.util.constant;

import org.apache.commons.lang.StringUtils;

public enum DdlEventType {
	DDL_ALTER, DDL_CREATE, DDL_DROP, DDL_RENAME, DDL_TRUNCATE,
	/* default */
	DDL_DEFAULT;

	private static final String STR_ALTER = "ALTER";
	private static final String STR_CREATE = "CREATE";
	private static final String STR_DROP = "DROP";
	private static final String STR_RENAME = "RENAME";
	private static final String STR_TRUNCATE = "TRUNCATE";

	public static DdlEventType getEventType(String strPrefix) {
		if (StringUtils.isBlank(strPrefix)) {
			return DdlEventType.DDL_DEFAULT;
		}
		
		if (strPrefix.equalsIgnoreCase(STR_ALTER)) {
			return DdlEventType.DDL_ALTER;
		} else if (strPrefix.equalsIgnoreCase(STR_CREATE)) {
			return DdlEventType.DDL_CREATE;
		} else if (strPrefix.equalsIgnoreCase(STR_DROP)) {
			return DdlEventType.DDL_DROP;
		} else if (strPrefix.equalsIgnoreCase(STR_RENAME)) {
			return DdlEventType.DDL_RENAME;
		} else if (strPrefix.equalsIgnoreCase(STR_TRUNCATE)) {
			return DdlEventType.DDL_TRUNCATE;
		} else {
			return DdlEventType.DDL_DEFAULT;
		}
	}
}
