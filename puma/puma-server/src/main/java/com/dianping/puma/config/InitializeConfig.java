/**
 * 
 */
package com.dianping.puma.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import com.dianping.lion.client.BeanData;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;

/**
 * 此类覆盖lion，为了补充lion的不足，实现可以从System加载环境变量。
 */
public class InitializeConfig implements BeanFactoryPostProcessor, PriorityOrdered, BeanNameAware, BeanFactoryAware {

    private static Logger logger = Logger.getLogger(InitializeConfig.class);
    /** Default placeholder prefix: "${" */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
    /** Default placeholder suffix: "}" */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
    private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;
    private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;
    private int order = Ordered.LOWEST_PRECEDENCE; // default: same as non-Ordered
    private BeanFactory beanFactory;
    private String beanName;
    private String nullValue = "";
    protected String address;
    private String environment;
    private String propertiesPath;
    private Properties pts;
    private Map<String, BeanData> propertyMap = new HashMap<String, BeanData>();

    private String getProjectEnv() {
        try {
            Class<?> config = Class.forName("com.dianping.lion.EnvZooKeeperConfig");
            Method method = config.getMethod("getEnv");
            return (String) method.invoke(null);
        } catch (Exception e) {
            logger.error("Can't find class com.dianping.lion.EnvZooKeeperConfig", e);
            throw new RuntimeException(e);
        }
    }

    private String getZKAdress() {
        try {
            Class<?> config = Class.forName("com.dianping.lion.EnvZooKeeperConfig");
            Method method = config.getMethod("getZKAddress");
            return (String) method.invoke(null);
        } catch (Exception e) {
            logger.error("Can't find class com.dianping.lion.EnvZooKeeperConfig", e);
            throw new RuntimeException(e);
        }
    }

    public void init() throws IOException {
        this.pts = new Properties();
        if (this.propertiesPath != null) {
            final InputStream propIn = this.getClass().getClassLoader().getResourceAsStream(propertiesPath);
            this.pts.load(new InputStream() {
                boolean temp = false;

                public int read() throws IOException {
                    if (temp) {
                        temp = false;
                        return ':';
                    }
                    int result = propIn.read();
                    if (result == (int) ':') {
                        temp = true;
                        return '\\';
                    }
                    return result;
                }
            });
            propIn.close();
        }
        this.address = this.getZKAdress();
        logger.info(">>>>>>>>>>>Current ZooKeeper Address Is " + this.address);
        this.environment = this.getProjectEnv();
        logger.info(">>>>>>>>>>>Current Project Environment Is " + this.environment);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactoryToProcess) throws BeansException {
        try {
            init();
        } catch (IOException e1) {
            throw new BeansException("init properties error", e1) {
                private static final long serialVersionUID = 6866582836134406673L;
            };
        }
        /*
         * if(this.address == null || this.address.startsWith(DEFAULT_PLACEHOLDER_PREFIX)){ if(this.pts != null){ this.address =
         * this.pts.getProperty("lion.zk.address"); } }
         */
        StringValueResolver valueResolver;
        try {
            ConfigCache cache = ConfigCache.getInstance(this.address);
            List<String> keyList = new ArrayList<String>();
            if (!this.environment.equalsIgnoreCase("dev")) {
                for (Object key : pts.keySet()) {
                    String value = pts.getProperty((String) key);
                    if (!(value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX))) {
                        keyList.add((String) key);
                    }
                }
            }
            for (String key : keyList) {
                this.pts.remove(key);
                logger.info("Environment :" + environment + "! Ignore Key Config In ApplicationContext! Key: " + (String) key);
            }
            cache.setPts(this.pts);
            valueResolver = new PlaceholderResolvingStringValueResolver(cache);
        } catch (LionException e) {
            throw new BeansException("new instance ConfigCache error", e) {
                private static final long serialVersionUID = -1546036309824048670L;
            };
        }
        BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);
        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file locations.
            if (!(beanNames[i].equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanNames[i]);
                try {
                    // cache beanName and fieldName
                    MutablePropertyValues mpvs = bd.getPropertyValues();
                    PropertyValue[] pvs = mpvs.getPropertyValues();
                    if (pvs != null) {
                        for (PropertyValue pv : pvs) {
                            Object value = pv.getValue();
                            if (value instanceof TypedStringValue) {
                                String value_ = ((TypedStringValue) value).getValue();
                                if (value_.startsWith(this.placeholderPrefix) && value_.endsWith(this.placeholderSuffix)) {
                                    value_ = value_.substring(2);
                                    value_ = value_.substring(0, value_.length() - 1);
                                    this.propertyMap.put(value_, new BeanData(beanNames[i], pv.getName()));
                                }
                            }
                        }
                    }
                    visitor.visitBeanDefinition(bd);
                } catch (BeanDefinitionStoreException ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanNames[i], ex.getMessage());
                }
            }
        }
        // New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
        beanFactoryToProcess.resolveAliases(valueResolver);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * Parse the given String value recursively, to be able to resolve nested placeholders (when resolved property values in turn
     * contain placeholders again).
     * 
     * @param strVal the String value to parse
     * @param props the Properties to resolve placeholders against
     * @param visitedPlaceholders the placeholders that have already been visited during the current resolution attempt (used to
     *            detect circular references between placeholders). Only non-null if we're parsing a nested placeholder.
     * @throws BeanDefinitionStoreException if invalid values are encountered
     * @see #resolvePlaceholder(String, java.util.Properties, int)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected String parseStringValue(String strVal, ConfigCache cache, Set visitedPlaceholders)
            throws BeanDefinitionStoreException {
        StringBuffer buf = new StringBuffer(strVal);
        int startIndex = strVal.indexOf(this.placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(buf, startIndex);
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                if (!visitedPlaceholders.add(placeholder)) {
                    throw new BeanDefinitionStoreException("Circular placeholder reference '" + placeholder
                            + "' in property definitions");
                }
                // Recursive invocation, parsing placeholders contained in the placeholder key.
                placeholder = parseStringValue(placeholder, cache, visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                String propVal = null;
                String placeholder_ = null;
                if (this.pts != null) {
                    placeholder_ = this.pts.getProperty(placeholder);
                }
                if (placeholder_ != null) {
                    if (placeholder_.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && placeholder_.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
                        placeholder_ = placeholder_.substring(2);
                        placeholder_ = placeholder_.substring(0, placeholder_.length() - 1);
                        try {
                            propVal = cache.getProperty(placeholder_);
                            placeholder = placeholder_;
                        } catch (LionException e) {
                            throw new BeanDefinitionStoreException("get config error", e);
                        }
                    } else {
                        if (this.environment.equalsIgnoreCase("dev")) {
                            propVal = placeholder_;
                            logger.info(">>>>>>>>>>>>getProperty key from applicationContext: " + placeholder_ + "  value:"
                                    + propVal);
                        } else {
                            try {
                                logger.warn(">>>>>>>>>>>>please delete key from applicationContext, Key:" + placeholder);
                                propVal = cache.getProperty(placeholder);
                                logger.info(">>>>>>>>>>>>getProperty key from applicationContext: " + placeholder + "  value:"
                                        + propVal);
                            } catch (LionException e) {
                                throw new BeanDefinitionStoreException("get config error", e);
                            }
                        }
                    }
                } else {
                    try {
                        propVal = cache.getProperty(placeholder);
                    } catch (LionException e) {
                        throw new BeanDefinitionStoreException("get config error", e);
                    }
                }
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(propVal, cache, visitedPlaceholders);
                    buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                    startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                } else {
                    //修改lion之处：如果propVal实在为null，则尝试从System环境变量加载
                    propVal = resolveSystemProperty(placeholder);
                    if (propVal != null) {
                        logger.info(">>>>>>>>>>>>getProperty key from SysntemProperty: " + placeholder + "  value:" + propVal);
                        // Recursive invocation, parsing placeholders contained in the
                        // previously resolved placeholder value.
                        propVal = parseStringValue(propVal, cache, visitedPlaceholders);
                        buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                        startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                    } else {
                        throw new BeanDefinitionStoreException("Could not resolve placeholder '" + placeholder + "'");
                    }
                }
                visitedPlaceholders.remove(placeholder);
            } else {
                startIndex = -1;
            }
        }
        return buf.toString();
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            } else if (StringUtils.substringMatch(buf, index, this.placeholderPrefix)) {
                withinNestedPlaceholder++;
                index = index + this.placeholderPrefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }

    /**
     * BeanDefinitionVisitor that resolves placeholders in String values, delegating to the <code>parseStringValue</code> method of
     * the containing class.
     */
    @SuppressWarnings("rawtypes")
    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {
        private final ConfigCache cache;

        public PlaceholderResolvingStringValueResolver(ConfigCache cache) {
            this.cache = cache;
            this.cache.addChange(new BeanConfigChange());
        }

        public String resolveStringValue(String strVal) throws BeansException {
            String value = parseStringValue(strVal, this.cache, new HashSet());
            return (value.equals(nullValue) ? null : value);
        }
    }

    /**
     * @param address the address to set
     */
    /*
     * public void setAddress(String address) { this.address = address; }
     */

    /**
     * @return the propertiesPath
     */
    public String getPropertiesPath() {
        return propertiesPath;
    }

    /**
     * @param propertiesPath the propertiesPath to set
     */
    public void setPropertiesPath(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    private class BeanConfigChange implements ConfigChange {

        @Override
        public void onChange(String key, String value) {
            BeanData bd = InitializeConfig.this.propertyMap.get(key);
            if (bd != null) {
                Object bean = InitializeConfig.this.beanFactory.getBean(bd.getBeanName());
                if (bean != null) {
                    String fieldName = bd.getFieldName();
                    String methodName = "set" + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1, fieldName.length());
                    Method method = null;
                    try {
                        method = bean.getClass().getDeclaredMethod(methodName, new Class[] { String.class });
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (method == null) {
                        Field field = null;
                        try {
                            field = bean.getClass().getField(fieldName);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        if (field == null) {
                            logger.error("no field to set property:" + fieldName);
                        } else {
                            field.setAccessible(true);
                            try {
                                field.set(bean, value);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    } else {
                        method.setAccessible(true);
                        try {
                            method.invoke(bean, value);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    protected String resolveSystemProperty(String key) {
        try {
            String value = System.getProperty(key);
            if (value == null) {
                value = System.getenv(key);
            }
            return value;
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not access system property '" + key + "': " + ex);
            }
            return null;
        }
    }
}
