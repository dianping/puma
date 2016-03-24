package com.dianping.puma.core.util.sql;

public enum DDLType {

	ALTER_DATABASE(1),
	ALTER_EVENT(2),
	ALTER_LOGFILE_GROUP(3),
	ALTER_FUNCTION(4),
	ALTER_PROCEDURE(5),
	ALTER_SERVER(6),
	ALTER_TABLE(7),
	ALTER_TABLESPACE(8),
	ALTER_VIEW(9),

	CREATE_DATABASE(11),
	CREATE_EVENT(12),
	CREATE_INDEX(13),
	CREATE_LOGFILE_GROUP(14),
	CREATE_FUNCTION(15),
	CREATE_PROCEDURE(16),
	CREATE_SERVER(17),
	CREATE_TABLE(18),
	CREATE_TABLESPACE(19),
	CREATE_TRIGGER(20),
	CREATE_VIEW(21),

	DROP_DATABASE(31),
	DROP_EVENT(32),
	DROP_INDEX(33),
	DROP_LOGFILE_GROUP(34),
	DROP_FUNCTION(35),
	DROP_PROCEDURE(36),
	DROP_SERVER(37),
	DROP_TABLE(38),
	DROP_TABLESPACE(39),
	DROP_TRIGGER(40),
	DROP_VIEW(41),

	RENAME_DATABASE(51),
	RENAME_TABLE(52),

	TRUNCATE_TABLE(61);
	
	private int type;
	
	DDLType(int type){
		this.type = type;
	}
	
	
	public int getDDLType(){
		return this.type;
	}
	
	public static DDLType getDDLType(int type){
		for(DDLType ddlType : DDLType.values()){
			if(ddlType.getDDLType() == type){
				return ddlType;
			}
		}
		
		return null;
	}
}
