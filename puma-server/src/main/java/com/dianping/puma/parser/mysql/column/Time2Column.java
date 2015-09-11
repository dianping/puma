package com.dianping.puma.parser.mysql.column;

public class Time2Column implements Column {

	private static final long serialVersionUID = 8034832395703193652L;
	
	private final String value;

	public Time2Column(String value){
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString(){
		return value;
	}
	
	public static final Time2Column valueOf(String value ){
		return new Time2Column(value);
	}
	
}
