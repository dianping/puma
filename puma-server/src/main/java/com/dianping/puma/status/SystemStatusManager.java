package com.dianping.puma.status;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.status.SystemStatus.Client;
import com.dianping.puma.status.SystemStatus.Server;

public class SystemStatusManager {

	public static SystemStatus status = new SystemStatus();

	private static ConcurrentMap<String, AtomicBoolean> stopTheWorlds = new ConcurrentHashMap<String, AtomicBoolean>();

	public static void addClient(String clientName, String database, List<String> tables, boolean withDml, boolean withDdl,
	      boolean withTransaction, String codec) {
		Client client = new Client(clientName, database, tables, withDml, withDdl, withTransaction, codec);

		status.getClients().put(clientName, client);
	}

	public static void addServer(String name, String host, int port, String database) {
		Server server = new Server(name, host, port, database);

		status.getServers().put(name, server);
	}

	public static void deleteClient(String clientName) {
		status.getClients().remove(clientName);
	}

	public static void deleteServer(String name) {
		status.getServers().remove(name);
	}

	public static void incServerDdlCounter(String name) {
		Server server = status.getServers().get(name);

		server.getTotalDdlEvent().incrementAndGet();
	}

	public static void incServerParsedCounter(String name) {
		Server server = status.getServers().get(name);

		server.getTotalParsedEvent().incrementAndGet();
	}

	public static void incServerRowDeleteCounter(String name) {
		Server server = status.getServers().get(name);

		server.getTotalDeleteEvent().incrementAndGet();
	}

	public static void incServerRowInsertCounter(String name) {
		Server server = status.getServers().get(name);

		server.getTotalInsertEvent().incrementAndGet();
	}

	public static void incServerRowUpdateCounter(String name) {
		Server server = status.getServers().get(name);

		server.getTotalUpdateEvent().incrementAndGet();
	}

	public static void incServerStoredBytes(String name, long size) {
		Server server = status.getServers().get(name);

		server.incStoreCountAndByte(size);
	}

	public static boolean isStopTheWorld(String serverName) {
		if (serverName == null) {
			return false;
		}
		AtomicBoolean stopTheWorld = stopTheWorlds.get(serverName);
		return (stopTheWorld != null) ? stopTheWorld.get() : false;
	}

	public static void startTheWorld(String serverName) {
		stopTheWorlds.put(serverName, new AtomicBoolean(false));
	}

	public static void stopTheWorld(String serverName) {
		stopTheWorlds.put(serverName, new AtomicBoolean(true));
	}

	public static void updateClientSendBinlogInfo(String clientName, BinlogInfo binlogInfo) {
		if(binlogInfo!= null) {
			Client client = status.getClients().get(clientName);

			if(client != null){
				client.setSendBinlogInfo(binlogInfo);
			}
		}
	}

	public static void updateClientAckBinlogInfo(String clientName, BinlogInfo binlogInfo) {
		if(binlogInfo!= null) {
			Client client = status.getClients().get(clientName);
			if(client != null) {
				client.setAckBinlogInfo(binlogInfo);
			}
		}
	}

	public static void updateServerBinlog(String name, String binlogFile, long binlogPosition) {
		Server server = status.getServers().get(name);

		server.updateBinlog(binlogFile, binlogPosition);
	}

	public static void updateServerBucket(String name, int bucketDate, int bucketNumber) {
		Server server = status.getServers().get(name);

		server.updateBucket(bucketDate, bucketNumber);
	}
}
