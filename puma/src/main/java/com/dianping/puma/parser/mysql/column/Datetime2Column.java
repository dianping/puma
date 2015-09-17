package com.dianping.puma.parser.mysql.column;

public class Datetime2Column implements Column{

	private static final long serialVersionUID = 4965439112415813099L;
	
	private final String value;

	public Datetime2Column(String value){
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
	
	public static final Datetime2Column valueOf(String value ){
		return new Datetime2Column(value);
	}
}
