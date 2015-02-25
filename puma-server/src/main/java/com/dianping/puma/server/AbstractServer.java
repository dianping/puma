/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-21
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.server;

import java.util.List;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.entity.replication.ReplicationTaskStatus;
import com.dianping.puma.core.monitor.Notifiable;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.replicate.model.task.StatusActionType;
import com.dianping.puma.core.replicate.model.task.StatusExecutorType;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.Dispatcher;

/**
 * TODO Comment of AbstractServer
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public abstract class AbstractServer implements Server, Notifiable {
    private PumaContext            context;
    private String                 defaultBinlogFileName;
    private Long                   defaultBinlogPosition;
    protected Parser               parser;
    protected DataHandler          dataHandler;
    protected Dispatcher           dispatcher;
    private long                   serverId;
    protected NotifyService        notifyService;
    private volatile boolean       stop     = false;
    protected BinlogPositionHolder binlogPositionHolder;

    protected String               name;

    protected ReplicationTaskStatus.Status taskStatus;
    protected StatusActionType statusActionType;
    
    protected StatusExecutorType statusExecutorType;

    /**
     * @param binlogPositionHolder
     *            the binlogPositionHolder to set
     */
    public void setBinlogPositionHolder(BinlogPositionHolder binlogPositionHolder) {
        this.binlogPositionHolder = binlogPositionHolder;
    }

    /**
     * @param notifyService
     *            the notifyService to set
     */
    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
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
     *            the defaultBinlogPosition to set
     */
    public void setDefaultBinlogPosition(Long defaultBinlogPosition) {
        this.defaultBinlogPosition = defaultBinlogPosition;
    }

    /**
     * @param parser
     *            the parser to set
     */
    public void setParser(Parser parser) {
        this.parser = parser;
    }

    /**
     * @param dataHandler
     *            the dataHandler to set
     */
    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    /**
     * @param dispatcher
     *            the dispatcher to set
     */
    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @return the serverId
     */
    public long getServerId() {
        return serverId;
    }

    /**
     * @param serverId
     *            the serverId to set
     */
    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.server.Server#getServerName()
     */
    @Override
    public String getServerName() {
        return name;
    }

    public String getName() {
   	return name;
   }

	public void setName(String name) {
   	this.name = name;
   }

	/*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.server.Server#stop()
     */
    @Override
    public void stop() throws Exception {
        doStop();
        stop = true;

        parser.stop();
        dataHandler.stop();
        dispatcher.stop();
    }

    public boolean isStop() {
        return stop;
    }

    protected abstract void doStop() throws Exception;

    protected abstract void doStart() throws Exception;

    public void start() throws Exception {
        stop = false;
        doStart();
    }

    @Override
	public void setStatusActionType(StatusActionType statusActionType) {
		this.statusActionType = statusActionType;
	}

    @Override
	public StatusActionType getStatusActionType() {
		return statusActionType;
	}

    @Override
    public List<Sender> getFileSender(){
    	return dispatcher.getSenders();
    }

    public ReplicationTaskStatus.Status getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(ReplicationTaskStatus.Status taskStatus) {
        this.taskStatus = taskStatus;
    }
}
