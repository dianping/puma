package com.dianping.puma.server;

import com.dianping.puma.server.impl.ReplicationBasedServer;

public class Bootstrap {

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {
		ReplicationBasedServer rbs = new ReplicationBasedServer();
		setServer(rbs);
		startServer(rbs);
		stopServer(rbs);

	}

	// read from config file
	public static void setServer(ReplicationBasedServer rbs) {
		rbs.setHost("192.168.7.43");
		rbs.setPort(3306);
		rbs.setUser("binlog");
		rbs.setPassword("binlog");
		rbs.setBinlogFileName("mysql-bin.000006");
		rbs.setBinlogPosition(4);

	}

	public static void startServer(final ReplicationBasedServer rbs)
			throws Exception {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					rbs.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();

	}

	public static void stopServer(ReplicationBasedServer rbs) throws Exception {
		rbs.stop();
	}

}
