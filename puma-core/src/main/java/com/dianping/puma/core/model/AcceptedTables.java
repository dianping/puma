package com.dianping.puma.core.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class AcceptedTables {

	private List<String> tables;

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public List<String> getTables() {
		return tables;
	}
	
	public boolean isContains(String tableName) {
		if (tables != null && tables.size() != 0) {
			for (String table : tables) {
				if (StringUtils.equalsIgnoreCase(table, tableName)) {
					return true;
				}
			}
		}
		return false;
	}

	public String toString() {
		String result = StringUtils.EMPTY;
		for (String table : tables) {
			result += table + ",";
		}
		if (StringUtils.isNotBlank(result)) {
			result = StringUtils.removeEnd(result, ",");
		}
		return result;
	}

}
