import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Project: puma-server
 * 
 * File Created at 2012-7-8
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */

/**
 * TODO Comment of Test
 * 
 * @author Leo Liang
 * 
 */
public class Test {
	public static void main(String[] args) throws Exception {
		URL url = new URL("http://localhost:7862/puma/channel");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(3000);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");

		PrintWriter out = new PrintWriter(connection.getOutputStream());

		out.print("seq=-1&ddl=true&dml=false&ts=false&dt=cat.*&name=mainTest");

		out.close();

		System.out.println(connection.getHeaderFields());
	}
}
