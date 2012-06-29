package com.dianping.puma.filter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.client.TableChangedData;

public class DbTbEventFilter extends AbstractEventFilter {

	private Map<String, Boolean>	dbtbMap;

	public void setDbtbMap(List<DbTbList> dbtbList) {
		dbtbMap = new ConcurrentHashMap<String, Boolean>();
		for (int i = 0; i < dbtbList.size(); i++) {
			for (int j = 0; j < dbtbList.get(i).getTbNameList().size(); j++) {
				dbtbMap.put(dbtbList.get(i).getDbName().trim().toLowerCase() + "."
						+ dbtbList.get(i).getTbNameList().get(j).trim().toLowerCase(), true);
			}
			dbtbMap.put(dbtbList.get(i) + ".", true);
		}
	}

	protected boolean checkEvent(DataChangedEvent event) {

		if (event.getDatas() != null) {
			for (TableChangedData tableChangedData : event.getDatas()) {
				String dbName = StringUtils.trimToEmpty(tableChangedData.getMeta().getDatabase());
				String tbName = StringUtils.trimToEmpty(tableChangedData.getMeta().getTable());
				if (dbtbMap.get(dbName.toLowerCase() + "." + tbName.toLowerCase()) != null) {
					return true;
				}
			}
		} else {
			return false;
		}

		return false;
	}

}