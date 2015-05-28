package com.dianping.puma.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.dianping.puma.parser.mysql.column.StringColumn;

public class CharSetTest {
	public static void main(String []args){
		byte[] value = {-25, -82, -95, -25, -112, -122, -27, -111, -104, 52};
		System.out.println(StringColumn.valueOf(value));
		System.out.println(Charset.defaultCharset());
		try {
			System.out.println(new String(value ,"gb2312"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
