package com.dianping.puma.biz.dao.morphia.helper;

import com.dianping.puma.biz.sync.config.ConfigChangeListener;
import com.dianping.puma.biz.sync.config.DynamicConfig;
import com.dianping.puma.biz.sync.config.LionDynamicConfig;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个MongoCleint含有一个Mongo实例
 * 
 * @author wukezhu
 */
public class MongoClient implements ConfigChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(MongoClient.class);

    private static final String MONGO_SERVER_URI_KEYNAME = "puma.mongoServerUri";

    private static final String DB_NAME = "puma";
    
    private static MongoOptions mongoOptions;
    static {
        //读取properties配置(如果存在configFile，则使用configFile)
        InputStream in = MongoClient.class.getClassLoader().getResourceAsStream("puma-mongo.properties");
        MongoConfig config;
        if (in != null) {
            config = new MongoConfig(in);
        } else {
            config = new MongoConfig();
        }
        //构造mongo选项
        mongoOptions = getMongoOptions(config);
    }

    private DynamicConfig dynamicConfig;
    private Mongo mongo;
    private Datastore datastore;

    /**
     * 构造一个MongoClient, 服务器地址通过LionDynamicConfig自动获取。
     */
    public MongoClient() {
        this(null);
    }

    /**
     * 构造一个MongoClient, 参数dynamicConfig指定服务器地址。
     */
    public MongoClient(DynamicConfig dynamicConfig) {
        if (dynamicConfig == null) {
            //从动态配置中获取mongo服务器地址
            dynamicConfig = new LionDynamicConfig("puma-mongo.lion.properties");
        }
        this.dynamicConfig = dynamicConfig;
        this.dynamicConfig.setConfigChangeListener(this);
        List<ServerAddress> replicaSetSeeds = parseUriToAddressList(dynamicConfig.get(MONGO_SERVER_URI_KEYNAME));
        //创建mongo实例
        mongo = new Mongo(replicaSetSeeds, mongoOptions);
        //创建Datastore
        datastore = new Morphia().createDatastore(mongo, DB_NAME);
        //at application start
        //map classes before calling with morphia map* methods
        datastore.ensureIndexes(); //creates indexes from @Index annotations in your entities
        datastore.ensureCaps(); //creates capped collections from @Entity
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
                //创建Datastore
                datastore = new Morphia().createDatastore(mongo, DB_NAME);
                //at application start
                //map classes before calling with morphia map* methods
                datastore.ensureIndexes(); //creates indexes from @Index annotations in your entities
                datastore.ensureCaps(); //creates capped collections from @Entity
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

    public Datastore getDatastore() {
        return datastore;
    }

    public Mongo getMongo() {
        return mongo;
    }
    
}
