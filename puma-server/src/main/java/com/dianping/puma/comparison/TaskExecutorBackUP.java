package com.dianping.puma.comparison;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
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
public class TaskExecutorBackup {

	private static final Logger LOG = LoggerFactory.getLogger(TaskExecutorBackup.class);

	private volatile DataSource sourceDs;

	private volatile String sourceTable;

	private volatile DataSource targetDs;

	private volatile String targetTable;

	private volatile Set<String> columns;

	private volatile String primaryKeyName = "ID";

	private volatile String updateTimeKeyName = "UpdateTime";

	private volatile Set<String> keys;

	private volatile Date lastTime;

	private volatile long lastId;

	private volatile boolean stoped = true;

	protected final BlockingQueue<RowContext> retryQueue = new LinkedBlockingQueue<RowContext>(1000);

	protected final BlockingQueue<RowContext> sourceDsQueue = new LinkedBlockingQueue<RowContext>(1000);

	protected final BlockingQueue<RowContext> targetDsQueue = new LinkedBlockingQueue<RowContext>(1000);

	private final ExecutorService threadPool = Executors.newFixedThreadPool(6);

	public void start() {
		if (!stoped) {
			return;
		}
		stoped = false;

		threadPool.submit(new RetryWorker());
		threadPool.submit(new SourceFetcher());
		threadPool.submit(new TargetFetcher());
		threadPool.submit(new TargetFetcher());
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
			try {
				Thread.currentThread().setName("SourceFetcher-" + sourceTable);

				JdbcTemplate template = new JdbcTemplate(sourceDs);
				template.setIgnoreWarnings(true);
				String sql = String
					.format("select %s from %s where %s >= ? and %s < now() - INTERVAL 10 MINUTE and %s > ? limit 1000",
						Joiner.on(",").join(columns), sourceTable, updateTimeKeyName, updateTimeKeyName,
						primaryKeyName);

				while (true) {
					List<Map<String, Object>> rows;
					try {
						rows = template.queryForList(sql, lastTime, lastId);
					} catch (Exception e) {
						LOG.error("Fetch source failed!", e);
						continue;
					}

					for (Map<String, Object> row : rows) {
						RowContext context = new RowContext();
						context.setSource(row);
						sourceDsQueue.put(context);
					}

					if (rows.size() > 0) {
						lastId = ((Number) rows.get(rows.size() - 1).get(primaryKeyName)).longValue();
						lastTime = (Date) rows.get(rows.size() - 1).get(updateTimeKeyName);
					} else {
						Thread.sleep(60 * 1000);
					}
				}
			} catch (InterruptedException ignore) {
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public class TargetFetcher implements Runnable {
		@Override public void run() {
			try {
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
					context = sourceDsQueue.take();

					Object[] args = FluentIterable.from(orderedKeys).transform(new Function<String, Object>() {
						@Override public Object apply(String input) {
							return context.getSource().get(input);
						}
					}).toArray(Object.class);

					try {
						List<Map<String, Object>> rows = template.queryForList(sql, args);
						context.setTarget(rows.size() == 0 ? new HashMap<String, Object>() : rows.get(0));
					} catch (Exception e) {
						LOG.error("Fetch target failed!", e);
						context.setLastRetryTime(new Date().getTime());
						context.increaseTryTimes();
						retryQueue.put(context);
					}

					targetDsQueue.put(context);
				}
			} catch (InterruptedException ignore) {
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public class ComparisonWorker implements Runnable {
		@Override public void run() {
			try {
				while (true) {
					RowContext item = targetDsQueue.take();

					Map<String, Object> source = item.getSource();
					source.remove(primaryKeyName);
					Map<String, Object> target = item.getTarget();
					target.remove(primaryKeyName);

					MapDifference<String, Object> result = Maps
						.difference(source, target, new Equivalence<Object>() {
							@Override protected boolean doEquivalent(Object a, Object b) {
								if (a == null || b == null) {
									return a == null && b == null ? true : false;
								}

								if (a instanceof byte[]) {
									return Arrays.equals((byte[]) a, (byte[]) b);
								}

								return a.equals(b);
							}

							@Override protected int doHash(Object o) {
								return o.hashCode();
							}
						});

					if (!result.areEqual()) {
						System.out.println("'" + item.getSource().get("OrderID") + "',");
					}
				}
			} catch (InterruptedException ignore) {
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
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

	public void setUpdateTimeKeyName(String updateTimeKeyName) {
		this.updateTimeKeyName = updateTimeKeyName;
	}

	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}
}