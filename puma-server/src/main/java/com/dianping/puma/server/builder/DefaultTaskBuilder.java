package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.server.DefaultTaskExecutor;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import org.springframework.stereotype.Service;

import com.dianping.puma.server.TaskExecutor;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultTaskBuilder implements TaskBuilder {

	public TaskExecutor build(PumaTaskEntity pumaTask) {

		// Resources.
		List<SrcDbEntity> srcDbs = pumaTask.getSrcdbs();

		return null;
	}

}