package com.dianping.puma.core.util;

import com.dianping.puma.core.util.SimpleDdlParser.DdlResult;
import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;

public class SimpleDdlParserTest {

	
	public static void main(String []args){
		
		String sql="CREATE DATABASE IF NOT EXISTS infra";
		DdlResult result = SimpleDdlParser.getDdlResult(DdlEventType.DDL_CREATE, DdlEventSubType.DDL_CREATE_DATABASE, sql);
		
		System.out.println(""+result.getDatabase()+"    "+result.getTable());
		sql="CREATE TABLE IF NOT EXISTS infra.chk_masterha (`key` tinyint NOT NULL primary key,`val` int(10) " +
				"unsigned NOT NULL DEFAULT '0') engine=MyISAM ";
		result = SimpleDdlParser.getDdlResult(DdlEventType.DDL_CREATE, DdlEventSubType.DDL_CREATE_TABLE, sql);
		System.out.println(""+result.getDatabase()+"    "+result.getTable());
	}
}
