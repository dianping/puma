package com.dianping.puma.core.util.constant;

import org.apache.commons.lang.StringUtils;

public enum DdlEventType {
	DDL_ALTER(1), DDL_CREATE(2), DDL_DROP(3), DDL_RENAME(4), DDL_TRUNCATE(5),
	/* default */
	DDL_DEFAULT(6);

	private int type;

	private static final String STR_ALTER = "ALTER";

	private static final String STR_CREATE = "CREATE";

	private static final String STR_DROP = "DROP";

	private static final String STR_RENAME = "RENAME";

	private static final String STR_TRUNCATE = "TRUNCATE";

	public static DdlEventType getEventType(int type) {
		for (DdlEventType eventType : DdlEventType.values()) {
			if (eventType.getEventType() == type) {
				return eventType;
			}
		}

		return DDL_DEFAULT;
	}

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

	DdlEventType(int type) {
		this.type = type;
	}

	public int getEventType() {
		return this.type;
	}
}
