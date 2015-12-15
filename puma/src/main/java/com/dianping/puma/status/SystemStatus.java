package com.dianping.puma.status;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.model.TableSet;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SystemStatus {

    private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

    private Map<String, Server> servers = new ConcurrentHashMap<String, Server>();

    private double load;

    private int storeQps;

    private long totalParsedEvent;

    private long totalStoreBytes;

    private long totalStoreCount;

    private long totalInsertEvent;

    private long totalUpdateEvent;

    private long totalDeleteEvent;

    private long totalDdlEvent;

    private long generateTime;

    public Map<String, Client> getClients() {
        return clients;
    }

    public Map<String, Server> getServers() {
        return servers;
    }

    public void count() {
        countFetchQps();
        countStoreQps();
        countTotalDdlEvent();
        countTotalDeleteEvent();
        countTotalInsertEvent();
        countTotalUpdateEvent();
        countTotalStoreBytes();
        countTotalParsedEvent();
        countTotalStoreCount();
        countLoad();

        generateTime = System.currentTimeMillis();
    }

    public long getGenerateTime() {
        return generateTime;
    }

    private void countLoad() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        load = os.getSystemLoadAverage() / os.getAvailableProcessors();
    }

    private void countFetchQps() {
        for (Client server : this.clients.values()) {
            server.count();
        }
    }

    private void countStoreQps() {
        int storeQps = 0;

        for (Server server : this.servers.values()) {
            server.count();
            storeQps += server.getStoreQps();
        }

        this.storeQps = storeQps;
    }

    private void countTotalDdlEvent() {
        long totalDdlEvent = 0L;

        for (Server server : this.servers.values()) {
            totalDdlEvent += server.getTotalDdlEvent();
        }

        this.totalDdlEvent = totalDdlEvent;
    }

    private void countTotalDeleteEvent() {
        long totalDeleteEvent = 0L;

        for (Server server : this.servers.values()) {
            totalDeleteEvent += server.getTotalDeleteEvent();
        }

        this.totalDeleteEvent = totalDeleteEvent;
    }

    private void countTotalInsertEvent() {
        long totalInsertEvent = 0L;

        for (Server server : this.servers.values()) {
            totalInsertEvent += server.getTotalInsertEvent();
        }

        this.totalInsertEvent = totalInsertEvent;
    }

    private void countTotalParsedEvent() {
        long totalParsedEvent = 0L;

        for (Server server : this.servers.values()) {
            totalParsedEvent += server.getTotalParsedEvent();
        }

        this.totalParsedEvent = totalParsedEvent;
    }

    private void countTotalStoreBytes() {
        long totalStoreBytes = 0L;

        for (Server server : this.servers.values()) {
            totalStoreBytes += server.getTotalStoreBytes();
        }

        this.totalStoreBytes = totalStoreBytes;
    }

    private void countTotalStoreCount() {
        long totalStoreCount = 0L;

        for (Server server : this.servers.values()) {
            totalStoreCount += server.getTotalStoreCount();
        }

        this.totalStoreCount = totalStoreCount;
    }

    private void countTotalUpdateEvent() {
        long totalUpdateEvent = 0L;

        for (Server server : this.servers.values()) {
            totalUpdateEvent += server.getTotalUpdateEvent();
        }

        this.totalUpdateEvent = totalUpdateEvent;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public int getStoreQps() {
        return storeQps;
    }

    public long getTotalParsedEvent() {
        return totalParsedEvent;
    }

    public long getTotalStoreBytes() {
        return totalStoreBytes;
    }

    public long getTotalStoreCount() {
        return totalStoreCount;
    }

    public long getTotalInsertEvent() {
        return totalInsertEvent;
    }

    public long getTotalUpdateEvent() {
        return totalUpdateEvent;
    }

    public long getTotalDeleteEvent() {
        return totalDeleteEvent;
    }

    public long getTotalDdlEvent() {
        return totalDdlEvent;
    }

    public void setClients(Map<String, Client> clients) {
        this.clients = clients;
    }

    public void setServers(Map<String, Server> servers) {
        this.servers = servers;
    }

    public static class Client {
        private String ip;

        private String name;

        private String database;

        private List<String> tables;

        private boolean withDml;

        private boolean withDdl;

        private boolean withTransaction;

        private String codec;

        private String storage;

        private BinlogInfo sendBinlogInfo;

        private BinlogInfo ackBinlogInfo;

        private long fetchQps;

        private transient QpsCounter fetchQpsCounter;

        public Client(String name, String ip, String database, List<String> tables, boolean withDml, boolean withDdl,
                      boolean withTransaction, String codec) {
            super();
            this.name = name;
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
            return this.fetchQps;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public void increaseFetchQps(long size) {
            fetchQpsCounter.add(size);
        }

        public void count() {
            this.fetchQps = fetchQpsCounter.get(15);
        }

        public String getStorage() {
            return storage;
        }

        public Client setStorage(String storage) {
            this.storage = storage;
            return this;
        }
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

        private transient AtomicLong atomicTotalParsedEvent = new AtomicLong(0);

        private transient AtomicLong atomicTotalStoreCount = new AtomicLong(0);

        private transient AtomicLong atomicTotalStoreBytes = new AtomicLong(0);

        private transient AtomicLong atomicTotalInsertEvent = new AtomicLong(0);

        private transient AtomicLong atomicTotalUpdateEvent = new AtomicLong(0);

        private transient AtomicLong atomicTotalDeleteEvent = new AtomicLong(0);

        private transient AtomicLong atomicTotalDdlEvent = new AtomicLong(0);

        private transient QpsCounter storeQpsCounter;

        public Server(String name, String host, int port) {
            this(name, host, port, null);
        }

        public Server(String name, String host, int port, TableSet target) {
            super();
            this.name = name;
            this.host = host;
            this.port = port;
            this.target = target;
            this.storeQpsCounter = new QpsCounter(15);
        }

        public void count() {
            this.storeQps = storeQpsCounter.get(15);
        }

        public int getBucketDate() {
            return bucketDate;
        }

        public int getBucketNumber() {
            return bucketNumber;
        }

        public TableSet getTarget() {
            return target;
        }

        public void setTarget(TableSet target) {
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
            return this.storeQps;
        }

        public void incStoreCountAndByte(long size) {
            this.totalStoreCount = this.atomicTotalStoreCount.incrementAndGet();
            this.totalStoreBytes = this.atomicTotalStoreBytes.addAndGet(size);
            this.storeQpsCounter.increase();
        }

        public void incStoreCount() {
            totalStoreCount = atomicTotalStoreCount.incrementAndGet();
            storeQpsCounter.increase();
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

        public long getTotalParsedEvent() {
            return totalParsedEvent;
        }

        public long getTotalStoreCount() {
            return totalStoreCount;
        }

        public long getTotalStoreBytes() {
            return totalStoreBytes;
        }

        public long getTotalInsertEvent() {
            return totalInsertEvent;
        }

        public long getTotalUpdateEvent() {
            return totalUpdateEvent;
        }

        public long getTotalDeleteEvent() {
            return totalDeleteEvent;
        }

        public long getTotalDdlEvent() {
            return totalDdlEvent;
        }

        public void increaseTotalDdlEvent() {
            this.totalDdlEvent = this.atomicTotalDdlEvent.incrementAndGet();
        }

        public void increaseTotalParsedEvent() {
            this.totalParsedEvent = this.atomicTotalParsedEvent.incrementAndGet();

        }

        public void increaseTotalDeleteEvent() {
            this.totalDeleteEvent = this.atomicTotalDeleteEvent.incrementAndGet();

        }

        public void increaseTotalInsertEvent() {
            this.totalInsertEvent = this.atomicTotalInsertEvent.incrementAndGet();
        }

        public void increaseTotalUpdateEvent() {
            this.totalUpdateEvent = this.atomicTotalUpdateEvent.incrementAndGet();
        }
    }
}
