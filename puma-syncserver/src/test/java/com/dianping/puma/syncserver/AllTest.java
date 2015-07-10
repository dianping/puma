package com.dianping.puma.syncserver;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.biz.entity.sync.mapping.DatabaseMapping;
import com.dianping.puma.biz.entity.sync.mapping.MysqlMapping;
import com.dianping.puma.biz.entity.sync.mapping.TableMapping;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.syncserver.job.transform.DefaultTransformer;
import com.dianping.puma.syncserver.job.transform.Transformer;
import org.junit.Before;
import org.junit.Test;

public class AllTest {

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

		transformer.init();
		transformer.start();
	}

	@Test
	public void testSqlDdl() {
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setSeq(1);
		ddlEvent.setBinlogInfo(new BinlogInfo("mysql-bin.000001", 1L, 0));
		ddlEvent.setDatabase("all-test-from-database");
		ddlEvent.setTable("all-test-from-table");
		ddlEvent.setDDLType(DDLType.ALTER_TABLE);
		ddlEvent.setSql("ALTER TABLE `all-test-from-table` ADD `name` VARCHAR(20)");

		transformer.transform(ddlEvent);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
