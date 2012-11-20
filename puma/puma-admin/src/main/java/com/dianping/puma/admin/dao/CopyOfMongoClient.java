package com.dianping.puma.admin.dao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.admin.config.ConfigChangeListener;
import com.dianping.puma.admin.config.DynamicConfig;
import com.dianping.puma.admin.config.impl.LionDynamicConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * 一个MongoCleint含有一个Mongo实例
 * 
 * @author wukezhu
 */
public class CopyOfMongoClient implements ConfigChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(CopyOfMongoClient.class);

    private static final String MONGO_SERVER_URI_KEYNAME = "mongoServerUri";

    private static final String DB_NAME = "puma_admin";

    private static final String SIZE_KEYNAME = null;

    private static final int MILLION = 0;

    private static final String MAX_DOC_NUM_KEYNAME = null;

    private static MongoOptions mongoOptions;
    static {
        //读取properties配置(如果存在configFile，则使用configFile)
        InputStream in = CopyOfMongoClient.class.getClassLoader().getResourceAsStream("puma-admin-mongo.properties");
        MongoConfig config;
        if (in != null) {
            config = new MongoConfig(in);
        } else {
            config = new MongoConfig();
        }
        //构造mongo选项
        mongoOptions = getMongoOptions(config);
    }

    /** 由于DBCollection创建后不会删除，故可以缓存DBCollection，避免db.collectionExists和避免db.getCollection的调用 */
    private final Map<String, DBCollection> cachedCollections = new ConcurrentHashMap<String, DBCollection>();
    private DynamicConfig dynamicConfig;
    private Mongo mongo;

    /**
     * 构造一个MongoClient, 服务器地址通过LionDynamicConfig自动获取。
     */
    public CopyOfMongoClient() {
        this(null);
    }

    /**
     * 构造一个MongoClient, 参数dynamicConfig指定服务器地址。
     */
    public CopyOfMongoClient(DynamicConfig dynamicConfig) {
        if (dynamicConfig == null) {
            //从动态配置中获取mongo服务器地址
            dynamicConfig = new LionDynamicConfig("puma-admin-mongo.lion.properties");
        }
        this.dynamicConfig = dynamicConfig;
        this.dynamicConfig.setConfigChangeListener(this);
        List<ServerAddress> replicaSetSeeds = parseUriToAddressList(dynamicConfig.get(MONGO_SERVER_URI_KEYNAME));
        //创建mongo实例
        mongo = new Mongo(replicaSetSeeds, mongoOptions);
    }

    @Override
    public synchronized void onConfigChange(String key, String value) {
        if (LOG.isInfoEnabled()) {
            LOG.info("onChange() called.");
        }
        value = value.trim();
        try {
            if (MONGO_SERVER_URI_KEYNAME.equals(key)) {
                //mongo地址变化，重新构造mongo
                String mongoServerUri = dynamicConfig.get(MONGO_SERVER_URI_KEYNAME);
                List<ServerAddress> replicaSetSeeds = parseUriToAddressList(mongoServerUri);
                mongo = new Mongo(replicaSetSeeds, mongoOptions);
            }
        } catch (Exception e) {
            LOG.error("Error occour when reset config from Lion, no config property would changed :" + e.getMessage(), e);
        }
    }

    private List<ServerAddress> parseUriToAddressList(String uri) {
        uri = uri.trim();
        String schema = "mongodb://";
        if (uri.startsWith(schema)) { // 兼容老各式uri
            uri = uri.substring(schema.length());
        }
        String[] hostPortArr = uri.split(",");
        List<ServerAddress> result = new ArrayList<ServerAddress>();
        for (int i = 0; i < hostPortArr.length; i++) {
            String[] pair = hostPortArr[i].split(":");
            try {
                result.add(new ServerAddress(pair[0].trim(), Integer.parseInt(pair[1].trim())));
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + ". Bad format of mongo uri：" + uri
                        + ". The correct format is mongodb://<host>:<port>,<host>:<port>", e);
            }
        }
        return result;
    }

    private static MongoOptions getMongoOptions(MongoConfig config) {
        MongoOptions options = new MongoOptions();
        options.slaveOk = config.isSlaveOk();
        options.socketKeepAlive = config.isSocketKeepAlive();
        options.socketTimeout = config.getSocketTimeout();
        options.connectionsPerHost = config.getConnectionsPerHost();
        options.threadsAllowedToBlockForConnectionMultiplier = config.getThreadsAllowedToBlockForConnectionMultiplier();
        options.w = config.getW();
        options.wtimeout = config.getWtimeout();
        options.fsync = config.isFsync();
        options.connectTimeout = config.getConnectTimeout();
        options.maxWaitTime = config.getMaxWaitTime();
        options.autoConnectRetry = config.isAutoConnectRetry();
        options.safe = config.isSafe();
        return options;
    }

    public DBCollection getCollection(String collectionName, DBObject indexDBObject) {
        DBCollection collection = this.cachedCollections.get(collectionName);
        if (collection == null) {
            DB db = mongo.getDB(DB_NAME);
            synchronized (collectionName.intern()) {
                collection = this.cachedCollections.get(collectionName);
                if (collection == null && !db.collectionExists(collectionName)) {
                    collection = createColletcion(collectionName, indexDBObject);
                    this.cachedCollections.put(collectionName, collection);//缓存collection
                }
            }
            if (collection == null) {
                collection = db.getCollection(collectionName);
                this.cachedCollections.put(collectionName, collection);//缓存collection
            }
        }
        return collection;
    }

    /**
     * 创建一个DBCollection
     */
    private DBCollection createColletcion(String collectionName, DBObject indexDBObject) {
        DBObject options = new BasicDBObject();
        options.put("capped", true);
        options.put("size", Integer.parseInt(this.dynamicConfig.get(SIZE_KEYNAME)) * MILLION);//max db file size in bytes
        options.put("max", Integer.parseInt(this.dynamicConfig.get(MAX_DOC_NUM_KEYNAME)) * MILLION);//max row count
        try {
            DBCollection collection = this.mongo.getDB(DB_NAME).createCollection(collectionName, options);
            LOG.info("Create collection '" + collection + "' on db " + DB_NAME + ", index is " + indexDBObject);
            if (indexDBObject != null) {
                collection.ensureIndex(indexDBObject);
                LOG.info("Ensure index " + indexDBObject + " on colleciton " + collection);
            }
            return collection;
        } catch (MongoException e) {
            if (e.getMessage() != null && e.getMessage().indexOf("collection already exists") >= 0) {
                //collection already exists
                LOG.warn(e.getMessage() + ":the collectionName is " + collectionName);
                return this.mongo.getDB(DB_NAME).getCollection(collectionName);
            } else {
                //other exception, can not connect to mongo etc, should abort
                throw e;
            }
        }
    }

}
