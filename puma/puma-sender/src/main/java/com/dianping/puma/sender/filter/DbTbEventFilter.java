package com.dianping.puma.sender.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.client.ChangedEvent;

public class DbTbEventFilter extends AbstractEventFilter {

	private Map<String, Boolean>	dbtbMap				= new ConcurrentHashMap<String, Boolean>();
	private List<String>			prefixList			= new ArrayList<String>();
	private static final String		DB_TB_SPLIT_STR		= ".";
	private static final String		ANY_SUFIX_PATTERN	= "*";

	public void setDbtbMap(List<DbTbList> dbtbList) {
		for (int i = 0; i < dbtbList.size(); i++) {
			for (int j = 0; j < dbtbList.get(i).getTbNameList().size(); j++) {
				String tbName = dbtbList.get(i).getTbNameList().get(j).trim().toLowerCase();
				String dbName = dbtbList.get(i).getDbName().trim().toLowerCase();
				if (!tbName.endsWith(ANY_SUFIX_PATTERN)) {
					dbtbMap.put(dbName + DB_TB_SPLIT_STR + tbName, true);
				} else {
					if (ANY_SUFIX_PATTERN.length() < tbName.length()) {
						prefixList.add(dbName + DB_TB_SPLIT_STR
								+ tbName.substring(0, tbName.length() - ANY_SUFIX_PATTERN.length()));
					} else if (ANY_SUFIX_PATTERN.length() == tbName.length()) {
						prefixList.add(dbName + DB_TB_SPLIT_STR);
					}

				}

			}
			dbtbMap.put(dbtbList.get(i).getDbName() + DB_TB_SPLIT_STR, true);
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
				for (String prefix : prefixList) {
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