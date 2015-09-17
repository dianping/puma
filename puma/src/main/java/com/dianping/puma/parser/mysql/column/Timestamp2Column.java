package com.dianping.puma.parser.mysql.column;

public class Timestamp2Column implements Column {
	
	private static final long serialVersionUID = 9055763180467573271L;
	private final String value;

	public Timestamp2Column(String value){
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
	
	public static final Timestamp2Column valueOf(String value ){
		return new Timestamp2Column(value);
	}

}
