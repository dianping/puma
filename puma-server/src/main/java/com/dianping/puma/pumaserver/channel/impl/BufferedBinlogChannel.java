package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;

import java.util.List;
import java.util.concurrent.*;

public class BufferedBinlogChannel implements BinlogChannel {

	private volatile boolean stopped = true;

	private ExecutorService executorService;

	private EventChannel eventChannel;

	private TaskContainer taskContainer;

	private BlockingQueue<Event> binlogBuffer;

	@Override
	public void init(
			String targetName,
			long dbServerId,
			long sc,
			BinlogInfo binlogInfo,
			long timestamp,
			String database,
			List<String> tables,
			boolean dml,
			boolean ddl,
			boolean transaction
	) throws BinlogChannelException {

		EventStorage eventStorage = taskContainer.getTaskStorage(targetName);

		if (eventStorage == null) {
			throw new BinlogChannelException("find event storage failure, not exist.");
		}

		try {
			eventChannel = eventStorage.getChannel(
					sc,
					dbServerId,
					binlogInfo.getBinlogFile(),
					binlogInfo.getBinlogPosition(),
					timestamp
			);
			eventChannel.withDatabase(database);
			eventChannel.withTables(tables.toArray(new String[tables.size()]));
			eventChannel.withDml(dml);
			eventChannel.withDdl(ddl);
			eventChannel.withTransaction(transaction);
			eventChannel.open();

			binlogBuffer = new ArrayBlockingQueue<Event>(1000);

			executorService = Executors.newFixedThreadPool(1);
			executorService.execute(extractTask);

		} catch (Exception e) {
			throw new BinlogChannelException("find event storage failure", e.getCause());
		}
	}

	private Runnable extractTask = new Runnable() {
		@Override
		public void run() {
			while (!stopped) {
				Event binlogEvent;

				try {
					binlogEvent = eventChannel.next();
				} catch (Exception e) {
					binlogEvent = new ServerErrorEvent("get binlog event from storage failure.", e.getCause());
				}

				try {
					binlogBuffer.put(binlogEvent);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					stopped = true;
				}
			}
		}
	};

	@Override
	public void destroy() {
		stopped = true;

		executorService.shutdown();

		eventChannel.close();

		binlogBuffer.clear();
		binlogBuffer = null;
	}

	@Override
	public Event next() {
		Event binlogEvent = null;

		try {
			binlogEvent = binlogBuffer.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			stopped = true;
		}

		return binlogEvent;
	}

	@Override
	public Event next(long timeout, TimeUnit timeUnit) {
		Event binlogEvent = null;

		try {
			binlogEvent = binlogBuffer.poll(timeout, timeUnit);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			stopped = true;
		}

		return binlogEvent;
	}

	public void setTaskContainer(TaskContainer taskContainer) {
		this.taskContainer = taskContainer;
	}
}
