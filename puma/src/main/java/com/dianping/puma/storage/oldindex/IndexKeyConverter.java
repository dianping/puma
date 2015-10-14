package com.dianping.puma.storage.oldindex;

public class IndexKeyConverter implements IndexItemConverter<IndexKeyImpl> {

	@Override
	public IndexKeyImpl convertFromObj(Object value) {
		IndexKeyImpl l1Index = new IndexKeyImpl();

		if (value instanceof String) {
			String[] splits = ((String) value).split("!");

			if (splits.length == 4) {
				l1Index.setTimestamp(Long.parseLong(splits[0]));
				l1Index.setServerId(Long.parseLong(splits[1]));
				l1Index.setBinlogFile(splits[2]);
				l1Index.setBinlogPosition(Long.parseLong(splits[3]));
			} else {
				// never come here
				return null;
			}
		}

		return l1Index;
	}

	@Override
	public String convertToObj(IndexKeyImpl value) {
		return value.getTimestamp() + "!" + value.getServerId() + "!" + value.getBinlogFile() + "!"
		      + value.getBinlogPosition();
	}
}
