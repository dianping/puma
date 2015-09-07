package com.dianping.puma.comparison;

import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Date;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutorTest {

	@Test
	public void fetchSourceDebug() throws Exception {
		final TaskExecutor target = new TaskExecutor();
		GroupDataSource ds = new GroupDataSource();
		ds.setJdbcRef("unifiedorder0");
		ds.init();

		target.setColumns(Sets.newHashSet("*"));
		target.setSourceTable("UOD_Order0");
		target.setLastTime(new Date(1441036800000l));
		target.setSourceDs(ds);

		new Thread(new Runnable() {
			@Override public void run() {
				long fetchSize = 0;

				while (true) {
					try {
						RowContext item = target.sourceDsQueue.take();
						System.out.println(++fetchSize);
					} catch (InterruptedException ignore) {
						break;
					}
				}
			}
		}).start();

		TaskExecutor.SourceFetcher sourceFetcher = target.new SourceFetcher();
		sourceFetcher.run();
	}
}