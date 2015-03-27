package com.dianping.puma.core.util;

import org.apache.commons.lang.StringUtils;

public class StringUtilsTest {
	
	public static void main(String []args){
		String strSql="Alter database table content ";
		String result = StringUtils.substringBetween(strSql, " ", " ");
		System.out.println(result);
	}
}
