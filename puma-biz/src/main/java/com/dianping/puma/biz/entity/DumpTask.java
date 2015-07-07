package com.dianping.puma.biz.entity;


import com.dianping.puma.biz.sync.model.mapping.DumpMapping;

import java.util.Arrays;
import java.util.List;

public class DumpTask extends BaseSyncTask {

	private DumpMapping dumpMapping;

	private List<String> options = Arrays.asList("--no-autocommit", "--disable-keys", "--quick",
			"--add-drop-database=false", "--no-create-info", "--add-drop-table=false", "--skip-add-locks",
			"--default-character-set=utf8", "--max_allowed_packet=16777216", "--net_buffer_length=16384",
			"-i", "--master-data=2", "--single-transaction", "--hex-blob");

	public DumpMapping getDumpMapping() {
		return dumpMapping;
	}

	public void setDumpMapping(DumpMapping dumpMapping) {
		this.dumpMapping = dumpMapping;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}
}
