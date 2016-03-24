package com.dianping.puma.portal.config;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static Config instance;

    public static Config getInstance() {
        return instance;
    }

    private String errorCodeHandler;

    private Map<ErrorCode, List<String>> errorCodeHandlerMap = new HashMap<ErrorCode, List<String>>();

    @PostConstruct
    public void init() {
        try {
            String[] splits = StringUtils.split(errorCodeHandler, ';');
            if (splits != null) {
                for (String split : splits) {
                    String[] splits2 = StringUtils.split(split, ',');
                    if (splits2 != null) {
                        Integer errorCode = Integer.parseInt(splits2[0]);
                        String desc = splits2[1];
                        String handler = splits2[2];
                        ErrorCode ec = new ErrorCode(errorCode, desc);
                        List<String> handlers = errorCodeHandlerMap.get(ec);
                        if (handlers == null) {
                            handlers = new ArrayList<String>();
                            errorCodeHandlerMap.put(ec, handlers);
                        }
                        handlers.add(handler);
                    }
                }
            }
        } catch (RuntimeException e) {
            LOG.error("Error Property 'errorCodeHandler'", e);
        }
        LOG.info("Properties: " + this.toString());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public void setErrorCodeHandler(String errorCodeHandler) {
        this.errorCodeHandler = errorCodeHandler;
    }

    public Map<ErrorCode, List<String>> getErrorCodeHandlerMap() {
        return errorCodeHandlerMap;
    }

    public void setErrorCodeHandlerMap(Map<ErrorCode, List<String>> errorCodeHandlerMap) {
        this.errorCodeHandlerMap = errorCodeHandlerMap;
    }

    public String getErrorCodeHandler() {
        return errorCodeHandler;
    }

    @Override
    public String toString() {
        return "Config [errorCodeHandler=" + errorCodeHandler + ", errorCodeHandlerMap=" + errorCodeHandlerMap + "]";
    }

    public static class ErrorCode {
        private Integer errorCode;
        private String desc;

        private ErrorCode(Integer errorCode, String desc) {
            super();
            this.errorCode = errorCode;
            this.desc = desc;
        }

        public Integer getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(Integer errorCode) {
            this.errorCode = errorCode;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((errorCode == null) ? 0 : errorCode.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof ErrorCode))
                return false;
            ErrorCode other = (ErrorCode) obj;
            if (errorCode == null) {
                if (other.errorCode != null)
                    return false;
            } else if (!errorCode.equals(other.errorCode))
                return false;
            return true;
        }

    }

}
