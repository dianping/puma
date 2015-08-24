package com.dianping.puma.admin.model;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

import java.util.List;
import java.util.Map;

/**
 * Dozer @ 15/8/24
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class PumaServerStatusDto {
    private Map<String, Client> clients;

    private Map<String, Server> servers;

    private int storeQps;

    private long totalParsedEvent;

    private long totalStoreBytes;

    private long totalStoreCount;

    private long totalInsertEvent;

    private long totalUpdateEvent;

    private long totalDeleteEvent;

    private long totalDdlEvent;

    public Map<String, Client> getClients() {
        return clients;
    }

    public void setClients(Map<String, Client> clients) {
        this.clients = clients;
    }

    public Map<String, Server> getServers() {
        return servers;
    }

    public void setServers(Map<String, Server> servers) {
        this.servers = servers;
    }

    public int getStoreQps() {
        return storeQps;
    }

    public void setStoreQps(int storeQps) {
        this.storeQps = storeQps;
    }

    public long getTotalParsedEvent() {
        return totalParsedEvent;
    }

    public void setTotalParsedEvent(long totalParsedEvent) {
        this.totalParsedEvent = totalParsedEvent;
    }

    public long getTotalStoreBytes() {
        return totalStoreBytes;
    }

    public void setTotalStoreBytes(long totalStoreBytes) {
        this.totalStoreBytes = totalStoreBytes;
    }

    public long getTotalStoreCount() {
        return totalStoreCount;
    }

    public void setTotalStoreCount(long totalStoreCount) {
        this.totalStoreCount = totalStoreCount;
    }

    public long getTotalInsertEvent() {
        return totalInsertEvent;
    }

    public void setTotalInsertEvent(long totalInsertEvent) {
        this.totalInsertEvent = totalInsertEvent;
    }

    public long getTotalUpdateEvent() {
        return totalUpdateEvent;
    }

    public void setTotalUpdateEvent(long totalUpdateEvent) {
        this.totalUpdateEvent = totalUpdateEvent;
    }

    public long getTotalDeleteEvent() {
        return totalDeleteEvent;
    }

    public void setTotalDeleteEvent(long totalDeleteEvent) {
        this.totalDeleteEvent = totalDeleteEvent;
    }

    public long getTotalDdlEvent() {
        return totalDdlEvent;
    }

    public void setTotalDdlEvent(long totalDdlEvent) {
        this.totalDdlEvent = totalDdlEvent;
    }

    public static class Server {

        private String name;

        private String host;

        private int port;

        private TableSet target;

        private BinlogInfo binlogInfo;

        private int bucketDate;

        private int bucketNumber;

        private long storeQps;

        private long totalParsedEvent;

        private long totalStoreCount;

        private long totalStoreBytes;

        private long totalInsertEvent;

        private long totalUpdateEvent;

        private long totalDeleteEvent;

        private long totalDdlEvent;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public TableSet getTarget() {
            return target;
        }

        public void setTarget(TableSet target) {
            this.target = target;
        }

        public BinlogInfo getBinlogInfo() {
            return binlogInfo;
        }

        public void setBinlogInfo(BinlogInfo binlogInfo) {
            this.binlogInfo = binlogInfo;
        }

        public int getBucketDate() {
            return bucketDate;
        }

        public void setBucketDate(int bucketDate) {
            this.bucketDate = bucketDate;
        }

        public int getBucketNumber() {
            return bucketNumber;
        }

        public void setBucketNumber(int bucketNumber) {
            this.bucketNumber = bucketNumber;
        }

        public long getStoreQps() {
            return storeQps;
        }

        public void setStoreQps(long storeQps) {
            this.storeQps = storeQps;
        }

        public long getTotalParsedEvent() {
            return totalParsedEvent;
        }

        public void setTotalParsedEvent(long totalParsedEvent) {
            this.totalParsedEvent = totalParsedEvent;
        }

        public long getTotalStoreCount() {
            return totalStoreCount;
        }

        public void setTotalStoreCount(long totalStoreCount) {
            this.totalStoreCount = totalStoreCount;
        }

        public long getTotalStoreBytes() {
            return totalStoreBytes;
        }

        public void setTotalStoreBytes(long totalStoreBytes) {
            this.totalStoreBytes = totalStoreBytes;
        }

        public long getTotalInsertEvent() {
            return totalInsertEvent;
        }

        public void setTotalInsertEvent(long totalInsertEvent) {
            this.totalInsertEvent = totalInsertEvent;
        }

        public long getTotalUpdateEvent() {
            return totalUpdateEvent;
        }

        public void setTotalUpdateEvent(long totalUpdateEvent) {
            this.totalUpdateEvent = totalUpdateEvent;
        }

        public long getTotalDeleteEvent() {
            return totalDeleteEvent;
        }

        public void setTotalDeleteEvent(long totalDeleteEvent) {
            this.totalDeleteEvent = totalDeleteEvent;
        }

        public long getTotalDdlEvent() {
            return totalDdlEvent;
        }

        public void setTotalDdlEvent(long totalDdlEvent) {
            this.totalDdlEvent = totalDdlEvent;
        }
    }

    public static class Client {
        private String ip;

        private String database;

        private List<String> tables;

        private boolean withDml;

        private boolean withDdl;

        private boolean withTransaction;

        private String codec;

        private BinlogInfo sendBinlogInfo;

        private BinlogInfo ackBinlogInfo;

        private long fetchQps;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public List<String> getTables() {
            return tables;
        }

        public void setTables(List<String> tables) {
            this.tables = tables;
        }

        public boolean isWithDml() {
            return withDml;
        }

        public void setWithDml(boolean withDml) {
            this.withDml = withDml;
        }

        public boolean isWithDdl() {
            return withDdl;
        }

        public void setWithDdl(boolean withDdl) {
            this.withDdl = withDdl;
        }

        public boolean isWithTransaction() {
            return withTransaction;
        }

        public void setWithTransaction(boolean withTransaction) {
            this.withTransaction = withTransaction;
        }

        public String getCodec() {
            return codec;
        }

        public void setCodec(String codec) {
            this.codec = codec;
        }

        public BinlogInfo getSendBinlogInfo() {
            return sendBinlogInfo;
        }

        public void setSendBinlogInfo(BinlogInfo sendBinlogInfo) {
            this.sendBinlogInfo = sendBinlogInfo;
        }

        public BinlogInfo getAckBinlogInfo() {
            return ackBinlogInfo;
        }

        public void setAckBinlogInfo(BinlogInfo ackBinlogInfo) {
            this.ackBinlogInfo = ackBinlogInfo;
        }

        public long getFetchQps() {
            return fetchQps;
        }

        public void setFetchQps(long fetchQps) {
            this.fetchQps = fetchQps;
        }
    }
}
