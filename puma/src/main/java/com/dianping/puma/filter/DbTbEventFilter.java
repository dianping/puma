package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DbTbEventFilter extends AbstractEventFilter {

	private Map<String, Boolean> dbtbMap = new ConcurrentHashMap<String, Boolean>();

	private List<String> tbPrefixList = new ArrayList<String>();

	private static final String DB_TB_SPLIT_STR = ".";

	private static final String SUFFIX_ANY = "*";

	public void init(String[] dts) {
		if (dts != null && dts.length > 0) {
			for (String dt : dts) {
				String[] parts = dt.split("\\.");
				String dbName = parts[0].trim().toLowerCase();
				String tbName = parts[1].trim().toLowerCase();

				if (!tbName.endsWith(SUFFIX_ANY)) {
					dbtbMap.put(dbName + DB_TB_SPLIT_STR + tbName, true);
				} else {
					if (SUFFIX_ANY.length() < tbName.length()) {
						tbPrefixList
						      .add(dbName + DB_TB_SPLIT_STR + tbName.substring(0, tbName.length() - SUFFIX_ANY.length()));
					} else if (SUFFIX_ANY.length() == tbName.length()) {
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