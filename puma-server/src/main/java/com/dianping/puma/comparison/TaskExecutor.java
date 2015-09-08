package com.dianping.puma.comparison;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

	private volatile DataSource sourceDs;

	private volatile String sourceTable;

	private volatile DataSource targetDs;

	private volatile String targetTable;

	private volatile Set<String> columns;

	private volatile Set<String> keys;

	private volatile Date lastTime;

	private volatile boolean stoped = true;

	protected final BlockingQueue<RowContext> retryQueue = new LinkedBlockingQueue<RowContext>(1000);

	protected final BlockingQueue<RowContext> sourceDsQueue = new LinkedBlockingQueue<RowContext>(1000);

	protected final BlockingQueue<RowContext> targetDsQueue = new LinkedBlockingQueue<RowContext>(1000);

	private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

	public void start() {
		if (!stoped) {
			return;
		}
		stoped = false;

		threadPool.submit(new RetryWorker());
		threadPool.submit(new SourceFetcher());
		threadPool.submit(new TargetFetcher());
		threadPool.submit(new ComparisonWorker());

		while (!stoped) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ignore) {
				break;
			}
		}

		threadPool.shutdown();
	}

	public class RetryWorker implements Runnable {

		@Override public void run() {
			while (true) {

			}
		}
	}

	public class SourceFetcher implements Runnable {

		@Override public void run() {
			Thread.currentThread().setName("SourceFetcher-" + sourceTable);

			JdbcTemplate template = new JdbcTemplate(sourceDs);
			template.setIgnoreWarnings(true);
			int fetchMinute = 5;
			String sql = String.format("select %s from %s where updatetime >= ? and updatetime < ?",
				Joiner.on(",").join(columns), sourceTable);

			while (true) {
				if (new Date().getTime() - lastTime.getTime() < fetchMinute * 60 * 1000 * 2) {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException ignore) {
						return;
					}
					continue;
				}

				Date newTime = new Date(lastTime.getTime() + fetchMinute * 60 * 1000);
				List<Map<String, Object>> rows;
				try {
					rows = template.queryForList(sql, lastTime, newTime);
					lastTime = newTime;
				} catch (Exception e) {
					LOG.error("Fetch source failed!", e);
					continue;
				}

				fetchMinute = Math.min(15, Math.max(1, fetchMinute * 1000 / (rows.size() + 1)));
				for (Map<String, Object> row : rows) {
					RowContext context = new RowContext();
					context.setSource(row);

					try {
						sourceDsQueue.put(context);
					} catch (InterruptedException ignore) {
						return;
					}
				}
			}
		}
	}

	public class TargetFetcher implements Runnable {
		@Override public void run() {
			Thread.currentThread().setName("TargetFetcher-" + targetTable);

			JdbcTemplate template = new JdbcTemplate(targetDs);
			template.setIgnoreWarnings(true);
			LinkedList<String> orderedKeys = Lists.newLinkedList(keys);

			String sql = String.format("select %s from %s where %s limit 1",
				Joiner.on(",").join(columns), targetTable,
				Joiner.on(" and ").join(FluentIterable.from(orderedKeys).transform(new Function<String, String>() {
					@Override public String apply(String input) {
						return input + " = ?";
					}
				})));

			while (true) {
				final RowContext context;
				try {
					context = sourceDsQueue.take();
				} catch (InterruptedException ignore) {
					break;
				}

				Object[] args = FluentIterable.from(orderedKeys).transform(new Function<String, Object>() {
					@Override public Object apply(String input) {
						return context.getSource().get(input);
					}
				}).toArray(Object.class);

				try {
					List<Map<String, Object>> rows = template.queryForList(sql, args);
					context.setTarget(rows.size() == 0 ? null : rows.get(0));
				} catch (Exception e) {
					LOG.error("Fetch target failed!", e);
					context.setLastRetryTime(new Date().getTime());
					context.increaseTryTimes();
					try {
						retryQueue.put(context);
					} catch (InterruptedException ignore) {
						return;
					}
				}

				try {
					targetDsQueue.put(context);
				} catch (InterruptedException ignore) {
					return;
				}
			}
		}
	}

	public class ComparisonWorker implements Runnable {

		@Override public void run() {
			long fetchSize = 0;

			while (true) {
				try {
					RowContext item = targetDsQueue.take();
					System.out.println(++fetchSize);
				} catch (InterruptedException ignore) {
					break;
				}
			}
		}
	}

	public void stop() {
		stoped = true;
	}

	public void setSourceDs(DataSource sourceDs) {
		this.sourceDs = sourceDs;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public void setTargetDs(DataSource targetDs) {
		this.targetDs = targetDs;
	}

	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}

	public void setColumns(Set<String> columns) {
		this.columns = columns;
	}

	public void setKeys(Set<String> keys) {
		this.keys = keys;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}
}