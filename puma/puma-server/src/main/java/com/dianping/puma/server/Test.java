/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-11 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server;

/**
 * TODO Comment of Test
 * 
 * @author Leo Liang
 * 
 */
public class Test {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		//start();
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println("Connect!");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
		
		System.out.println("dddddd");
		System.out.println();
		
	
		
	}
	public static void start() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println("Connect");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
		
	}
}

