/**
 * Project: ${puma-server.aid}
 * <p/>
 * File Created at 2012-6-21
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.server;

import com.dianping.puma.biz.entity.TaskStateEntity;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.storage.holder.BinlogInfoHolder;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.Dispatcher;

import java.util.List;

/**
 * TODO Comment of AbstractServer
 *
 * @author Leo Liang
 */
@ThreadUnSafe
public abstract class AbstractTaskExecutor implements TaskExecutor {
	private PumaContext context;

	private String taskId;

	private long serverId;

	private String taskName;

	private String defaultBinlogFileName;

	private Long defaultBinlogPosition;

	protected Parser parser;

	protected DataHandler dataHandler;

	protected Dispatcher dispatcher;

	private volatile boolean stop = true;

	protected BinlogInfoHolder binlogInfoHolder;

	protected TaskStateEntity state;

	protected Status status;

	@Override
	public String getTaskId() {
		return taskId;
	}

	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	@Override
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @param binlogInfoHolder
	 *           the binlogPositionHolder to set
	 */
	public void setBinlogInfoHolder(BinlogInfoHolder binlogInfoHolder) {
		this.binlogInfoHolder = binlogInfoHolder;
	}

	public void setContext(PumaContext context) {
		this.context = context;
	}

	public PumaContext getContext() {
		return context;
	}

	public String getDefaultBinlogFileName() {
		return defaultBinlogFileName;
	}

	public void setDefaultBinlogFileName(String binlogFileName) {
		this.defaultBinlogFileName = binlogFileName;
	}

	/**
	 * @return the defaultBinlogPosition
	 */
	public Long getDefaultBinlogPosition() {
		return defaultBinlogPosition;
	}

	/**
	 * @param defaultBinlogPosition
	 *           the defaultBinlogPosition to set
	 */
	public void setDefaultBinlogPosition(Long defaultBinlogPosition) {
		this.defaultBinlogPosition = defaultBinlogPosition;
	}

	/**
	 * @param parser
	 *           the parser to set
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
	}

	/**
	 * @param dataHandler
	 *           the dataHandler to set
	 */
	public void setDataHandler(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	/**
	 * @param dispatcher
	 *           the dispatcher to set
	 */
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public boolean isStop() {
		return stop;
	}

	protected abstract void doStop() throws Exception;

	protected abstract void doStart() throws Exception;

	@Override
	public void start() throws Exception {
		stop = false;

		parser.start();
		dataHandler.start();
		dispatcher.start();
		doStart();
	}

	@Override
	public void stop() throws Exception {
		stop = true;

		parser.stop();
		dataHandler.stop();
		dispatcher.stop();

		doStop();
	}

	public void resume() throws Exception {
		stop = false;
	}

	public void pause() throws Exception {
		stop = true;
	}

	@Override
	public Status getStatus() {
		return this.state.getStatus();
	}

	@Override
	public void setStatus(Status status) {
		this.state.setStatus(status);
	}

	@Override
	public List<Sender> getFileSender() {
		return dispatcher.getSenders();
	}

	@Override
	public DataHandler getDataHandler() {
		return this.dataHandler;
	}

	public TaskStateEntity getTaskState() {
		return state;
	}

	public void setTaskState(TaskStateEntity state) {
		this.state = state;
	}

}
