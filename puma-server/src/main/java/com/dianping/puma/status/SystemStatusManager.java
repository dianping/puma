package com.dianping.puma.status;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.status.SystemStatus.Client;
import com.dianping.puma.status.SystemStatus.Server;
import com.google.common.base.Strings;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SystemStatusManager {

    public static SystemStatus status = new SystemStatus();

    private static ConcurrentMap<String, AtomicBoolean> stopTheWorlds = new ConcurrentHashMap<String, AtomicBoolean>();

    public static void addClient(String clientName, String database, List<String> tables, boolean withDml,
                                 boolean withDdl, boolean withTransaction, String codec) {
        Client client = new Client(clientName, database, tables, withDml, withDdl, withTransaction, codec);

        status.getClients().put(clientName, client);
    }

    public static void addServer(String name, String host, int port, TableSet target) {
        Server server = new Server(name, host, port, target);

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

        if (server != null) {
            server.getTotalDdlEvent().incrementAndGet();
        }
    }

    public static void incServerParsedCounter(String name) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.getTotalParsedEvent().incrementAndGet();
        }
    }

    public static void incServerRowDeleteCounter(String name) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.getTotalDeleteEvent().incrementAndGet();
        }
    }

    public static void incServerRowInsertCounter(String name) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.getTotalInsertEvent().incrementAndGet();
        }
    }

    public static void incServerRowUpdateCounter(String name) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.getTotalUpdateEvent().incrementAndGet();
        }
    }

    public static void incServerStoredBytes(String name, long size) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.incStoreCountAndByte(size);
        }
    }

    public static boolean isStopTheWorld(String serverName) {
        if (serverName == null) {
            return false;
        }
        AtomicBoolean stopTheWorld = stopTheWorlds.get(serverName);

        return (stopTheWorld != null) && stopTheWorld.get();
    }

    public static void startTheWorld(String serverName) {
        stopTheWorlds.put(serverName, new AtomicBoolean(false));
    }

    public static void stopTheWorld(String serverName) {
        stopTheWorlds.put(serverName, new AtomicBoolean(true));
    }

    public static void addClientFetchQps(String clientName, long size) {
        if (Strings.isNullOrEmpty(clientName)) {
            return;
        }
        Client client = status.getClients().get(clientName);
        if (client != null) {
            client.addFetchQps(size);
        }
    }

    public static void updateClientSendBinlogInfo(String clientName, BinlogInfo binlogInfo) {
        if (Strings.isNullOrEmpty(clientName) || binlogInfo == null) {
            return;
        }

        Client client = status.getClients().get(clientName);

        if (client != null) {
            client.setSendBinlogInfo(binlogInfo);
        }
    }

    public static void updateClientAckBinlogInfo(String clientName, BinlogInfo binlogInfo) {
        if (binlogInfo != null) {
            Client client = status.getClients().get(clientName);
            if (client != null) {
                client.setAckBinlogInfo(binlogInfo);
            }
        }
    }

    public static void updateServerBinlog(String name, BinlogInfo binlogInfo) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.setBinlogInfo(binlogInfo);
        }
    }

    public static void updateServerBucket(String name, int bucketDate, int bucketNumber) {
        Server server = status.getServers().get(name);

        if (server != null) {
            server.updateBucket(bucketDate, bucketNumber);
        }
    }
}
