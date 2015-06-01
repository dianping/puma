package com.dianping.puma.syncserver;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.syncserver.job.binlogmanage.MapDBBinlogManager;
import com.dianping.puma.syncserver.job.load.Loader;
import com.dianping.puma.syncserver.job.load.PooledLoader;
import com.dianping.puma.syncserver.job.load.exception.LoadException;
import com.dianping.puma.syncserver.job.transform.DefaultTransformer;
import com.dianping.puma.syncserver.job.transform.Transformer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class AllTest {

	Loader loader;
	Transformer transformer;

	@Before
	public void before() {
		DefaultTransformer defaultTransformer = new DefaultTransformer();
		defaultTransformer.setName("puma-test");
		MysqlMapping mysqlMapping = new MysqlMapping();
		DatabaseMapping databaseMapping = new DatabaseMapping();
		databaseMapping.setFrom("all-test-from-database");
		databaseMapping.setTo("all-test-to-database");
		TableMapping tableMapping = new TableMapping();
		tableMapping.setFrom("all-test-from-table");
		tableMapping.setTo("all-test-to-table");
		databaseMapping.addTable(tableMapping);
		mysqlMapping.addDatabase(databaseMapping);
		defaultTransformer.setMysqlMapping(mysqlMapping);
		transformer = defaultTransformer;

		PooledLoader pooledLoader = new PooledLoader();
		pooledLoader.setName("puma-test");
		pooledLoader.setHost("127.0.0.1");
		pooledLoader.setUsername("root");
		pooledLoader.setPassword("123456");
		pooledLoader.setConsistent(true);
		pooledLoader.setBatchRowPoolSize(100);
		pooledLoader.setBatchExecPoolSize(1);
		pooledLoader.setRetries(1);
		pooledLoader.setDelay(new AtomicLong());
		pooledLoader.setUpdates(new AtomicLong());
		pooledLoader.setInserts(new AtomicLong());
		pooledLoader.setDeletes(new AtomicLong());
		pooledLoader.setDdls(new AtomicLong());
		pooledLoader.setBinlogManager(new MapDBBinlogManager(0, new BinlogInfo("mysql-bin.000001", 0L)));
		loader = pooledLoader;

		loader.init();
		loader.start();

		transformer.init();
		transformer.start();
	}

	@Test
	public void testSqlDdl() {
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setSeq(1);
		ddlEvent.setBinlog("mysql-bin.000001");
		ddlEvent.setBinlogPos(1L);
		ddlEvent.setDatabase("all-test-from-database");
		ddlEvent.setTable("all-test-from-table");
		ddlEvent.setDDLType(DDLType.ALTER_TABLE);
		ddlEvent.setSql("ALTER TABLE `all-test-from-table` ADD `name` VARCHAR(20)");

		transformer.transform(ddlEvent);
		loader.load(ddlEvent);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
