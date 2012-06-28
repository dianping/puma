package com.dianping.puma.filter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.common.bo.PositionInfo;
import com.dianping.puma.common.bo.PumaContext;

public class DefaultEventFilter implements EventFilter {
	private static final Logger log = Logger
			.getLogger(EventFilterChainConfig.class);

	private static final String CONFIG = "EventFilter.xml";
	private static final String BEAN_DATABASES = "databases";
	private static final String FILTER_NAME = "DefaultEventFilter";

	private Map<String, Boolean> dbtbMap;
	
	
	public Map<String, Boolean> getDbtbMap() {
		return dbtbMap;
	}

	public void setDbtbMap(List<DbTbList> dbtbList) {
		dbtbMap= new ConcurrentHashMap<String, Boolean>();
		for(int i=0;i<dbtbList.size();i++)
			for(int j=0;j<dbtbList.get(i).getTbNameList().size();j++)
				dbtbMap.put(dbtbList.get(i).getDbName()+"."+dbtbList.get(i).getTbNameList().get(j), true);
	}

	

	public void doFilter(DataChangedEvent event,
			EventFilterChain eventfilterChain, PumaContext	context) {

		if (checkEvent(event)) {
			eventfilterChain.doNext(event, context);
			log.info("Accepted by " + FILTER_NAME + ".");
		}

	}

	public void initEventFilters() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(CONFIG);
		this.dbtbMap = (Map<String, Boolean>) ctx.getBean(BEAN_DATABASES);

		log.info("Load rules from " + FILTER_NAME + ".");

	}

	private boolean checkEvent(DataChangedEvent event) {
		// TODO

		return false;
	}

}