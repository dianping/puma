package com.dianping.puma.core.sync.dao;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责载入Mongo本地配置
 * 
 * @author wukezhu
 */
class MongoConfig {

   private static final Logger LOG                                          = LoggerFactory
                                                                                  .getLogger(MongoConfig.class);

   // local config(mongo server options)
   private boolean             slaveOk                                      = true;
   private boolean             socketKeepAlive                              = true;
   private int                 socketTimeout                                = 2000;
   private int                 connectionsPerHost                           = 30;
   private int                 threadsAllowedToBlockForConnectionMultiplier = 50;
   private int                 w                                            = 1;
   private int                 wtimeout                                     = 2000;
   private boolean             fsync                                        = false;
   private int                 connectTimeout                               = 2000;
   private int                 maxWaitTime                                  = 2000;
   private boolean             autoConnectRetry                             = true;
   private boolean             safe                                         = true;

   public MongoConfig() {
   }

   public MongoConfig(InputStream in) {
      loadLocalConfig(in);
   }

   @SuppressWarnings("rawtypes")
   private void loadLocalConfig(InputStream in) {
      Properties props = new Properties();
      try {
         props.load(in);
         in.close();
      } catch (IOException e1) {
         throw new RuntimeException(e1.getMessage(), e1);
      }

      Class clazz = this.getClass();
      for (String key : props.stringPropertyNames()) {
         Field field = null;
         try {
            field = clazz.getDeclaredField(key.trim());
         } catch (Exception e) {
            LOG.error("unknown property found: " + key);
            continue;
         }
         field.setAccessible(true);
         if (field.getType().equals(Integer.TYPE)) {
            try {
               field.set(this, Integer.parseInt(props.getProperty(key).trim()));
            } catch (Exception e) {
               LOG.error("can not parse property " + key, e);
               continue;
            }
         } else if (field.getType().equals(Long.TYPE)) {
            try {
               field.set(this, Long.parseLong(props.getProperty(key).trim()));
            } catch (Exception e) {
               LOG.error("can not set property " + key, e);
               continue;
            }
         } else if (field.getType().equals(String.class)) {
            try {
               field.set(this, props.getProperty(key).trim());
            } catch (Exception e) {
               LOG.error("can not set property " + key, e);
               continue;
            }
         } else {
            try {
               field.set(this, Boolean.parseBoolean(props.getProperty(key).trim()));
            } catch (Exception e) {
               LOG.error("can not set property " + key, e);
               continue;
            }
         }
      }

      if (LOG.isDebugEnabled()) {
         Field[] fields = clazz.getDeclaredFields();
         for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            if (!Modifier.isStatic(f.getModifiers())) {
               try {
                  LOG.debug(f.getName() + "=" + f.get(this));
               } catch (Exception e) {
               }
            }
         }
      }
   }

   public boolean isSlaveOk() {
      return slaveOk;
   }

   public boolean isSocketKeepAlive() {
      return socketKeepAlive;
   }

   public int getSocketTimeout() {
      return socketTimeout;
   }

   public int getConnectionsPerHost() {
      return connectionsPerHost;
   }

   public int getThreadsAllowedToBlockForConnectionMultiplier() {
      return threadsAllowedToBlockForConnectionMultiplier;
   }

   public int getW() {
      return w;
   }

   public int getWtimeout() {
      return wtimeout;
   }

   public boolean isFsync() {
      return fsync;
   }

   public int getConnectTimeout() {
      return connectTimeout;
   }

   public int getMaxWaitTime() {
      return maxWaitTime;
   }

   public boolean isAutoConnectRetry() {
      return autoConnectRetry;
   }

   public boolean isSafe() {
      return safe;
   }

}
