package com.dianping.puma.parser.meta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.meta.TableMetaInfo;
import com.dianping.puma.storage.BinlogIndexKey;
import com.dianping.puma.storage.BinlogIndexKeyConvertor;

/**
 * 
 * @author hao.zhu
 *
 */
public class TableMetaInfoStore {

	private static final String Path = "/data/appdatas/puma/meta";

	private static final String Suffix = "meta";

	private Map<String, TreeMap<BinlogIndexKey, TableMetaInfo>> tableMetaInfos = new ConcurrentHashMap<String, TreeMap<BinlogIndexKey, TableMetaInfo>>();

	private Map<String, BufferedWriter> writers = new ConcurrentHashMap<String, BufferedWriter>();

	private BinlogIndexKeyConvertor keyConvertor = new BinlogIndexKeyConvertor();

	private ObjectMapper om;

	public void start() throws IOException {
		om = new ObjectMapper();
		om.enableDefaultTyping();
		om.getSerializerProvider().setNullKeySerializer(new JsonSerializer<Object>() {
			@Override
			public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			      JsonProcessingException {
				jgen.writeFieldName("[NullKey]");
			}
		});

		File path = new File(Path);
		if (!path.exists()) {
			path.mkdirs();
		}

		for (File file : path.listFiles()) {
			String name = file.getName();

			int pos = name.indexOf('.');
			if (pos > 0) {
				name = name.substring(0, pos);
			} else {
				// never got here
				throw new IOException("illegal file name");
			}

			InputStream is = null;
			try {
				Properties prop = new Properties();
				is = new FileInputStream(file);
				prop.load(is);
				TreeMap<BinlogIndexKey, TableMetaInfo> meta = new TreeMap<BinlogIndexKey, TableMetaInfo>();
				for (String propName : prop.stringPropertyNames()) {
					BinlogIndexKey key = keyConvertor.convertFromString(propName);
					String value = prop.getProperty(propName);
					if (key != null && value != null) {
						TableMetaInfo tableMetaInfo = om.readValue(value, TableMetaInfo.class);

						meta.put(key, tableMetaInfo);
					}
				}

				tableMetaInfos.put(name, meta);
			} finally {
				closeQuietly(is);
			}
		}
	}

	public void addTableMetaInfo(DdlEvent event, TableMetaInfo tableMetaInfo) throws IOException {
		String fileName = tableMetaInfo.getDatabase() + "-" + tableMetaInfo.getTable();
		BinlogIndexKey dataIndexKey = new BinlogIndexKey(event.getBinlogInfo().getBinlogFile(), event.getBinlogInfo()
		      .getBinlogPosition(), event.getBinlogServerId());

		TreeMap<BinlogIndexKey, TableMetaInfo> treeMap = tableMetaInfos.get(fileName);
		if (treeMap != null) {
			if (treeMap.containsKey(dataIndexKey)) {
				return;
			}
		} else {
			treeMap = new TreeMap<BinlogIndexKey, TableMetaInfo>();
			tableMetaInfos.put(fileName, treeMap);
		}

		File file = new File(String.format("%s/%s.%s", Path, fileName, Suffix));
		if (!file.exists()) {
			file.createNewFile();
		}

		BufferedWriter bufferedWriter = writers.get(fileName);
		if (bufferedWriter == null) {
			bufferedWriter = new BufferedWriter(new FileWriter(file, true));
			writers.put(fileName, bufferedWriter);
		}

		bufferedWriter.write(keyConvertor.convertToString(dataIndexKey) + "=" + om.writeValueAsString(tableMetaInfo));
		bufferedWriter.newLine();
		bufferedWriter.flush();

		treeMap.put(dataIndexKey, tableMetaInfo);
	}

	public TableMetaInfo getTableMetaInfo(RowChangedEvent event) {
		String key = convertToKey(event);
		BinlogIndexKey indexKey = new BinlogIndexKey(event.getBinlogInfo().getBinlogFile(), event.getBinlogInfo()
		      .getBinlogPosition(), event.getBinlogServerId());

		TreeMap<BinlogIndexKey, TableMetaInfo> treeMap = tableMetaInfos.get(key);

		if (treeMap != null) {
			Entry<BinlogIndexKey, TableMetaInfo> ceilingEntry = treeMap.floorEntry(indexKey);

			if (ceilingEntry != null) {
				return ceilingEntry.getValue();
			} else {
				return null;
			}
		}

		return null;
	}

	public TableMetaInfo getMostRecentMetaInfo(String database, String table) {
		TreeMap<BinlogIndexKey, TableMetaInfo> treeMap = tableMetaInfos.get(database + "-" + table);

		if (treeMap != null) {
			Entry<BinlogIndexKey, TableMetaInfo> ceilingEntry = treeMap.lastEntry();

			if (ceilingEntry != null) {
				return ceilingEntry.getValue();
			} else {
				return null;
			}
		}

		return null;
	}

	public void stop() {
		for (BufferedWriter writer : writers.values()) {
			closeQuietly(writer);
		}
	}

	private String convertToKey(ChangedEvent event) {
		return event.getDatabase() + "-" + event.getTable();
	}

	private void closeQuietly(Writer out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception ignore) {
			}
		}
	}

	private void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
