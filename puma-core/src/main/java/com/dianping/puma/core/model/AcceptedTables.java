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
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AcceptedTables)){
			return false;
		}
		AcceptedTables m = (AcceptedTables)o;
		if(m.tables == null){
			if(this.tables != null)
				return false;
		}else{
			if(this.tables == null)
				return false;
			else{
				if(this.tables.size() == m.tables.size()){
					for(String table : m.tables){
						if(!this.tables.contains(table)){
							return false;
						}
					}
				}
			}
				
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		return tables.hashCode();
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
