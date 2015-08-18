package com.dianping.puma.status;

import com.dianping.puma.core.model.BinlogInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SystemStatus {

    private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

    private Map<String, Server> servers = new ConcurrentHashMap<String, Server>();

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

    public Map<String, Server> getServers() {
        return servers;
    }

    public int getStoreQps() {
        this.storeQps = 0;

        for (Server server : this.servers.values()) {
            this.storeQps += server.getStoreQps();
        }

        return storeQps;
    }

    public long getTotalDdlEvent() {
        this.totalDdlEvent = 0L;

        for (Server server : this.servers.values()) {
            this.totalDdlEvent += server.getTotalDdlEvent().get();
        }

        return totalDdlEvent;
    }

    public long getTotalDeleteEvent() {
        this.totalDeleteEvent = 0L;

        for (Server server : this.servers.values()) {
            this.totalDeleteEvent += server.getTotalDeleteEvent().get();
        }

        return totalDeleteEvent;
    }

    public long getTotalInsertEvent() {
        this.totalInsertEvent = 0L;

        for (Server server : this.servers.values()) {
            this.totalInsertEvent += server.getTotalInsertEvent().get();
        }

        return totalInsertEvent;
    }

    public long getTotalParsedEvent() {
        this.totalParsedEvent = 0L;

        for (Server server : this.servers.values()) {
            this.totalParsedEvent += server.getTotalParsedEvent().get();
        }

        return totalParsedEvent;
    }

    public long getTotalStoreBytes() {
        this.totalStoreBytes = 0L;

        for (Server server : this.servers.values()) {
            this.totalStoreBytes += server.getTotalStoreBytes().get();
        }

        return totalStoreBytes;
    }

    public long getTotalStoreCount() {
        this.totalStoreCount = 0L;

        for (Server server : this.servers.values()) {
            this.totalStoreCount += server.getTotalStoreCount().get();
        }

        return totalStoreCount;
    }

    public long getTotalUpdateEvent() {
        this.totalUpdateEvent = 0L;

        for (Server server : this.servers.values()) {
            this.totalUpdateEvent += server.getTotalUpdateEvent().get();
        }

        return totalUpdateEvent;
    }

    public void setClients(Map<String, Client> clients) {
        this.clients = clients;
    }

    public void setServers(Map<String, Server> servers) {
        this.servers = servers;
    }

    public void setStoreQps(int storeQps) {
        this.storeQps = storeQps;
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

        private QpsCounter fetchQpsCounter;

        public Client(String ip, String database, List<String> tables, boolean withDml, boolean withDdl,
                      boolean withTransaction, String codec) {
            super();
            this.ip = ip;
            this.database = database;
            this.tables = tables;
            this.withDml = withDml;
            this.withDdl = withDdl;
            this.withTransaction = withTransaction;
            this.codec = codec;
            this.fetchQpsCounter = new QpsCounter(15);
        }

        public String getCodec() {
            return codec;
        }

        public String getDatabase() {
            return database;
        }

        public long getFetchQps() {
            return this.fetchQpsCounter.get(15);
        }

        public String getIp() {
            return ip;
        }

        public List<String> getTables() {
            return tables;
        }

        public boolean isWithDdl() {
            return withDdl;
        }

        public boolean isWithDml() {
            return withDml;
        }

        public boolean isWithTransaction() {
            return withTransaction;
        }

        public BinlogInfo getAckBinlogInfo() {
            return ackBinlogInfo;
        }

        public void setAckBinlogInfo(BinlogInfo ackBinlogInfo) {
            this.ackBinlogInfo = ackBinlogInfo;
        }

        public BinlogInfo getSendBinlogInfo() {
            return sendBinlogInfo;
        }

        public void setSendBinlogInfo(BinlogInfo sendBinlogInfo) {
            this.sendBinlogInfo = sendBinlogInfo;
        }

        public void addFetchQps(long size) {
            fetchQpsCounter.add(size);
        }

        public void increaseFetQps() {
            fetchQpsCounter.increase();
        }
    }

    public static class Server {
        private String name;

        private String host;

        private int port;

        private String target;

        private BinlogInfo binlogInfo;

        private int bucketDate;

        private int bucketNumber;

        private AtomicLong totalParsedEvent = new AtomicLong(0);

        private AtomicLong totalStoreCount = new AtomicLong(0);

        private AtomicLong totalStoreBytes = new AtomicLong(0);

        private AtomicLong totalInsertEvent = new AtomicLong(0);

        private AtomicLong totalUpdateEvent = new AtomicLong(0);

        private AtomicLong totalDeleteEvent = new AtomicLong(0);

        private AtomicLong totalDdlEvent = new AtomicLong(0);

        private QpsCounter storeQpsCounter;

        public Server(String name, String host, int port) {
            this(name, host, port, null);
        }

        public Server(String name, String host, int port, String target) {
            super();
            this.name = name;
            this.host = host;
            this.port = port;
            this.target = target;
            this.storeQpsCounter = new QpsCounter(15);
        }

        public int getBucketDate() {
            return bucketDate;
        }

        public int getBucketNumber() {
            return bucketNumber;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getHost() {
            return host;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }

        public long getStoreQps() {
            return this.storeQpsCounter.get(15);
        }

        public AtomicLong getTotalDdlEvent() {
            return totalDdlEvent;
        }

        public AtomicLong getTotalDeleteEvent() {
            return totalDeleteEvent;
        }

        public AtomicLong getTotalInsertEvent() {
            return totalInsertEvent;
        }

        public AtomicLong getTotalParsedEvent() {
            return totalParsedEvent;
        }

        public AtomicLong getTotalStoreBytes() {
            return totalStoreBytes;
        }

        public AtomicLong getTotalStoreCount() {
            return totalStoreCount;
        }

        public AtomicLong getTotalUpdateEvent() {
            return totalUpdateEvent;
        }

        public void incStoreCountAndByte(long size) {
            this.totalStoreCount.incrementAndGet();
            this.totalStoreBytes.addAndGet(size);
            this.storeQpsCounter.increase();
        }

        public void setBucketDate(int bucketDate) {
            this.bucketDate = bucketDate;
        }

        public void setBucketNumber(int bucketOffset) {
            this.bucketNumber = bucketOffset;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public BinlogInfo getBinlogInfo() {
            return binlogInfo;
        }

        public void setBinlogInfo(BinlogInfo binlogInfo) {
            this.binlogInfo = binlogInfo;
        }

        public void updateBucket(int bucketDate, int bucketNumber) {
            this.bucketDate = bucketDate;
            this.bucketNumber = bucketNumber;
        }
    }
}
