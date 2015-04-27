package com.dianping.puma.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.event.ChangedEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.puma.core.event.Event;

public class DbTbEventFilter extends AbstractEventFilter {

	private static final Logger LOG = LoggerFactory.getLogger(DbTbEventFilter.class);

	private Map<String, Boolean> dbtbMap = new ConcurrentHashMap<String, Boolean>();

	private List<String> tbPrefixList = new ArrayList<String>();

	private static final String DB_TB_SPLIT_STR = ".";

	private static final String SUFIX_ANY = "*";

	public void init(String[] dts) {
		if (dts != null && dts.length > 0) {
			for (String dt : dts) {
				String[] parts = dt.split("\\.");
				String dbName = parts[0].trim().toLowerCase();
				String tbName = parts[1].trim().toLowerCase();

				if (!tbName.endsWith(SUFIX_ANY)) {
					dbtbMap.put(dbName + DB_TB_SPLIT_STR + tbName, true);
				} else {
					if (SUFIX_ANY.length() < tbName.length()) {
						tbPrefixList.add(dbName + DB_TB_SPLIT_STR
								+ tbName.substring(0, tbName.length() - SUFIX_ANY.length()));
					} else if (SUFIX_ANY.length() == tbName.length()) {
						tbPrefixList.add(dbName + DB_TB_SPLIT_STR);
					}

				}

				dbtbMap.put(dbName + DB_TB_SPLIT_STR, true);

			}
		}
	}

	protected boolean checkEvent(ChangedEvent event) {
		if (event != null) {
			String dbName = StringUtils.trimToEmpty(event.getDatabase()).toLowerCase();
			String tbName = StringUtils.trimToEmpty(event.getTable()).toLowerCase();
			String key = dbName + DB_TB_SPLIT_STR + tbName;
			if (dbtbMap.get(key) != null) {
				return true;
			} else {
				for (String prefix : tbPrefixList) {
					if (key.startsWith(prefix)) {
						return true;
					}
				}
				return false;
			}
		} else {
			return false;
		}
	}
}