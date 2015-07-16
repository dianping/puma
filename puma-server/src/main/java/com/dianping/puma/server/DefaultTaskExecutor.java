/**
 * Project: ${puma-server.aid}
 * <p/>
 * File Created at 2012-6-6 $Id$
 * <p/>
 * Copyright 2010 dianping.com. All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.datahandler.DataHandlerResult;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.QueryExecutor;
import com.dianping.puma.parser.mysql.ResultSet;
import com.dianping.puma.parser.mysql.UpdateExecutor;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.RotateEvent;
import com.dianping.puma.parser.mysql.packet.*;
import com.dianping.puma.server.exception.ServerEventFetcherException;
import com.dianping.puma.server.exception.ServerEventParserException;
import com.dianping.puma.server.exception.ServerEventRuntimeException;
import com.dianping.puma.status.SystemStatusContainer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于MySQL复制机制的Server
 *
 * @author Leo Liang
 */
@ThreadUnSafe
public class DefaultTaskExecutor extends AbstractTaskExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutor.class);

    private int port = 3306;

    private String dbHost;

    private String dbUsername;

    private long dbServerId;

    private String dbPassword;

    private String database;

    private String encoding = "utf-8";

	private Socket mysqlSocket;

    private InputStream is;

    private OutputStream os;

    @Override
    public void doStart() throws Exception {
        long failCount = 0;
        boolean canStop = false;
        do {
            try {
                // 读position/file文件
                BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(getContext().getPumaServerName());
                if (binlogInfo == null) {
                    binlogInfo = new BinlogInfo();
                    binlogInfo.setServerId(getContext().getDBServerId());
                    binlogInfo.setBinlogFile(getContext().getBinlogFileName());
                    binlogInfo.setBinlogPosition(getContext().getBinlogStartPos());
                }

                //todo: 将来要做处理，如果 binlog serverid 和 db serverid 不一样怎么办？
                getContext().setDBServerId(dbServerId);
                getContext().setBinlogFileName(binlogInfo.getBinlogFile());
                getContext().setBinlogStartPos(binlogInfo.getBinlogPosition());
                getContext().setMasterUrl(dbHost, port);

                setBinlogInfo(binlogInfo);

                SystemStatusContainer.instance.updateServerStatus(getTaskName(), dbHost, port, database, getContext()
                        .getBinlogFileName(), getContext().getBinlogStartPos());

                if (!connect()) {
                    throw new IOException("Connection failed.");
                }

                canStop = true;

                if (!auth()) {
                    throw new IOException("Login failed.");
                }

                if (getContext().isCheckSum()) {
                    if (!updateSetting()) {
                        throw new IOException("Update setting command failed.");
                    }
                }

                if (!queryBinlogFormat()) {
                    throw new IOException("Query config binlogformat failed.");
                }
                if (!queryBinlogImage()) {
                    throw new IOException("Query config binlog row image failed.");
                }

                if (dumpBinlog()) {
                    canStop = false;
                    processBinlog();
                } else {
                    throw new IOException("Binlog dump failed.");
                }
            } catch (Throwable e) {
                if (canStop) {
                    Cat.logError("Puma.server.failed", new ServerEventFetcherException("TaskName: " + getTaskName(), e));
                    stopTask();
                }
                if (e instanceof RuntimeException) {
                    Cat.logError("Puma.server.runtimeException", new ServerEventRuntimeException("TaskName: "
                            + getTaskName(), e));
                }
                if (isStop()) {
                    return;
                }
                if (++failCount % 3 == 0) {
                    failCount = 0;
                }
                LOG.error("Exception occurs. taskName: " + getTaskName() + " dbServerId: " + getDbServerId()
                        + ". Reconnect...", e);

                Thread.sleep(((failCount % 10) + 1) * 2000);
            }
        } while (!isStop());

    }

    private void processBinlog() throws IOException {
        while (!isStop()) {
            // only slow down parsing, not stop
            if (SystemStatusContainer.instance.isStopTheWorld(this.getTaskName())) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignore) {
                }
            }

            BinlogPacket binlogPacket = (BinlogPacket) PacketFactory.parsePacket(is, PacketType.BINLOG_PACKET,
                    getContext());

            if (!binlogPacket.isOk()) {
                LOG.error("TaskName: " + getTaskName() + ", Binlog packet response error.");
                throw new IOException("TaskName: " + getTaskName() + ", Binlog packet response error.");
            } else {
                processBinlogPacket(binlogPacket);
            }
        }
    }

    protected void processBinlogPacket(BinlogPacket binlogPacket) throws IOException {
        BinlogEvent binlogEvent = parser.parse(binlogPacket.getBinlogBuf(), getContext());
        if (binlogEvent.getHeader().getEventType() == BinlogConstants.INTVAR_EVENT
                || binlogEvent.getHeader().getEventType() == BinlogConstants.RAND_EVENT
                || binlogEvent.getHeader().getEventType() == BinlogConstants.USER_VAR_EVENT) {
            LOG.error("TaskName: " + getTaskName() + ", Binlog_format is MIXED or STATEMENT ,System is not support.");
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), dbHost, port);
            Cat.logEvent("Slave.dbBinlogFormat", eventName, "1", "");
            Cat.logError("Puma.server.mixedorstatement.format", new ServerEventParserException("TaskName: "
                    + getTaskName() + ", Binlog_format is MIXED or STATEMENT ,System is not support."));
            stopTask();
        }

        if (binlogEvent.getHeader().getEventType() != BinlogConstants.FORMAT_DESCRIPTION_EVENT) {
            getContext().setNextBinlogPos(binlogEvent.getHeader().getNextPosition());
        }

        if (binlogEvent.getHeader().getEventType() == BinlogConstants.ROTATE_EVENT) {
            processRotateEvent(binlogEvent);
        } else {
            processDataEvent(binlogEvent);
        }
    }

    protected void processDataEvent(BinlogEvent binlogEvent) {
        DataHandlerResult dataHandlerResult = null;
        // 一直处理一个binlogEvent的多行，处理完每行马上分发，以防止一个binlogEvent包含太多ChangedEvent而耗费太多内存
        int eventIndex = 0;
        do {
            dataHandlerResult = dataHandler.process(binlogEvent, getContext());
            if (dataHandlerResult != null && !dataHandlerResult.isEmpty()) {
                ChangedEvent changedEvent = dataHandlerResult.getData();

                changedEvent.getBinlogInfo().setEventIndex(eventIndex++);

                updateOpsCounter(changedEvent);

                dispatch(changedEvent);
            }
        } while (dataHandlerResult != null && !dataHandlerResult.isFinished());

        if (binlogEvent.getHeader().getEventType() != BinlogConstants.FORMAT_DESCRIPTION_EVENT) {
            getContext().setBinlogStartPos(binlogEvent.getHeader().getNextPosition());
            setBinlogInfo(new BinlogInfo(getBinlogInfo().getServerId(), getBinlogInfo().getBinlogFile(), binlogEvent.getHeader().getNextPosition()));
        }

        // status report
        SystemStatusContainer.instance.updateServerStatus(getTaskName(), dbHost, port, database, getContext()
                .getBinlogFileName(), getContext().getBinlogStartPos());

        // 只有整个binlogEvent分发完了才save ? TODO:如果分发到了一半挂了,重新启动后会回滚数据文件吗？
        if (binlogEvent.getHeader() != null
                && binlogEvent.getHeader().getNextPosition() != 0
                && StringUtils.isNotBlank(getContext().getBinlogFileName())
                && dataHandlerResult != null
                && !dataHandlerResult.isEmpty()
                && (dataHandlerResult.getData() instanceof DdlEvent || (dataHandlerResult.getData() instanceof RowChangedEvent && ((RowChangedEvent) dataHandlerResult
                .getData()).isTransactionCommit()))) {

            binlogInfoHolder.setBinlogInfo(getTaskName(), new BinlogInfo(getContext().getDBServerId(), getContext().getBinlogFileName(), binlogEvent
                    .getHeader().getNextPosition()));
        }
    }

    protected void dispatch(ChangedEvent changedEvent) {
        try {
            Cat.logEvent("Puma.Dispath", changedEvent.getDatabase());
            dispatcher.dispatch(changedEvent, getContext());
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", Dispatcher dispatch failed.", e);
        }
    }

    protected void updateOpsCounter(ChangedEvent changedEvent) {
        // 增加行变更计数器(除去ddl事件和事务信息事件)
        if ((changedEvent instanceof RowChangedEvent) && !((RowChangedEvent) changedEvent).isTransactionBegin()
                && !((RowChangedEvent) changedEvent).isTransactionCommit()) {
            switch (((RowChangedEvent) changedEvent).getActionType()) {
                case RowChangedEvent.INSERT:
                    incrRowsInsert();
                    SystemStatusContainer.instance.incServerRowInsertCounter(getTaskName());
                    break;
                case RowChangedEvent.UPDATE:
                    incrRowsUpdate();
                    SystemStatusContainer.instance.incServerRowUpdateCounter(getTaskName());
                    break;
                case RowChangedEvent.DELETE:
                    incrRowsDelete();
                    SystemStatusContainer.instance.incServerRowDeleteCounter(getTaskName());
                    break;
                default:
                    break;
            }
        } else if (changedEvent instanceof DdlEvent) {
            incrDdls();
            SystemStatusContainer.instance.incServerDdlCounter(getTaskName());
        }
    }

    protected void processRotateEvent(BinlogEvent binlogEvent) {
        RotateEvent rotateEvent = (RotateEvent) binlogEvent;
        binlogInfoHolder.setBinlogInfo(getTaskName(),
                new BinlogInfo(getContext().getDBServerId(), rotateEvent.getNextBinlogFileName(), rotateEvent.getFirstEventPosition()));
        getContext().setBinlogFileName(rotateEvent.getNextBinlogFileName());
        getContext().setBinlogStartPos(rotateEvent.getFirstEventPosition());

        setBinlogInfo(new BinlogInfo(getContext().getDBServerId(), rotateEvent.getNextBinlogFileName(), rotateEvent.getFirstEventPosition()));
        // status report
        SystemStatusContainer.instance.updateServerStatus(getTaskName(), dbHost, port, database, getContext()
                .getBinlogFileName(), getContext().getBinlogStartPos());
    }

	/**
	 * Connect to mysql master and parse the greeting packet
	 *
	 * @throws IOException
	 */
	private boolean connect() {
		try {
			closeTransport();
			this.mysqlSocket = new Socket();
			this.mysqlSocket.setTcpNoDelay(false);
			this.mysqlSocket.setKeepAlive(true);
			this.mysqlSocket.connect(new InetSocketAddress(dbHost, port));
			this.is = new BufferedInputStream(mysqlSocket.getInputStream());
			this.os = new BufferedOutputStream(mysqlSocket.getOutputStream());
			PacketFactory.parsePacket(is, PacketType.CONNECT_PACKET, getContext());

            LOG.info("TaskName: " + getTaskName() + ", Connection db success.");

            return true;
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", Connect failed. Reason: " + e.getMessage());

            return false;
        }
    }

    /**
     * Send COM_BINLOG_DUMP packet to mysql master and parse the response
     *
     * @return
     * @throws IOException
     */
    private boolean dumpBinlog() {
        try {
            ComBinlogDumpPacket dumpBinlogPacket = (ComBinlogDumpPacket) PacketFactory.createCommandPacket(
                    PacketType.COM_BINLOG_DUMP_PACKET, getContext());
            dumpBinlogPacket.setBinlogFileName(getContext().getBinlogFileName());
            dumpBinlogPacket.setBinlogFlag(0);
            dumpBinlogPacket.setBinlogPosition(getContext().getBinlogStartPos());
            dumpBinlogPacket.setServerId(getServerId());
            dumpBinlogPacket.buildPacket(getContext());

            dumpBinlogPacket.write(os, getContext());

            OKErrorPacket dumpCommandResultPacket = (OKErrorPacket) PacketFactory.parsePacket(is,
                    PacketType.OKERROR_PACKET, getContext());

            if (dumpCommandResultPacket.isOk()) {
                LOG.info("TaskName: " + getTaskName() + " ,Dump binlog command success.");

                return true;
            } else {
                LOG.error("TaskName: " + getTaskName() + " ,Dump binlog failed. Reason: "
                        + dumpCommandResultPacket.getMessage());

                return false;
            }
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + " Dump binlog failed. Reason: " + e.getMessage());

            return false;
        }

    }

    /**
     * Send Authentication Packet to mysql master and parse the response
     *
     * @return
     * @throws IOException
     */
    private boolean auth() {
        try {
            LOG.info("server logining taskName: " + getTaskName() + " host: " + dbHost + " port: " + port + " username: "
                    + dbUsername + " database: " + database + " dbServerId: " + getDbServerId());
            AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
                    PacketType.AUTHENTICATE_PACKET, getContext());

            authPacket.setPassword(dbPassword);
            authPacket.setUser(dbUsername);
            authPacket.setDatabase(database);
            authPacket.buildPacket(getContext());
            authPacket.write(os, getContext());

            OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(is, PacketType.OKERROR_PACKET,
                    getContext());
            boolean isAuth;

            if (okErrorPacket.isOk()) {
                LOG.info("TaskName: " + getTaskName() + ", Server login success.");
                isAuth = true;
            } else {
                isAuth = false;
                LOG.error("TaskName: " + getTaskName() + ", Login failed. Reason: " + okErrorPacket.getMessage());
            }

            return isAuth;
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", Login failed. Reason: " + e.getMessage());

            return false;
        }
    }

    /**
     * Send QueryCommand Packet to update binlog_checksum
     *
     * @return
     * @throws IOException
     */
    private boolean updateSetting() throws IOException {
        try {
            UpdateExecutor executor = new UpdateExecutor(is, os);
            String cmd = "set @master_binlog_checksum= '@@global.binlog_checksum'";
            OKErrorPacket okErrorPacket = executor.update(cmd, getContext());
            String eventStatus;
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), dbHost, port);
            if (okErrorPacket.isOk()) {
                eventStatus = Message.SUCCESS;
                LOG.info("TaskName: " + getTaskName() + ", Update setting command success.");
            } else {
                eventStatus = "1";
                LOG.error("TaskName: " + getTaskName() + ", UpdateSetting failed. Reason: " + okErrorPacket.getMessage());
            }

            Cat.logEvent("Slave.dbSetCheckSum", eventName, eventStatus, "");

            return okErrorPacket.isOk();
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", UpdateSetting failed. Reason: " + e.getMessage());

            return false;
        }
    }

    /**
     * Send QueryCommand Packet to query binlog_format
     *
     * @return
     * @throws IOException
     */
    private boolean queryBinlogFormat() throws IOException {
        try {
            QueryExecutor executor = new QueryExecutor(is, os);
            String cmd = "show global variables like 'binlog_format'";
            ResultSet rs = executor.query(cmd, getContext());
            List<String> columnValues = rs.getFiledValues();
            boolean isQuery = true;
            if (columnValues == null || columnValues.size() != 2 || columnValues.get(1) == null) {
                LOG.error("TaskName: " + getTaskName()
                        + ", QueryConfig failed Reason:unexcepted binlog format query result.");
                isQuery = false;
            }
            BinlogFormat binlogFormat = BinlogFormat.valuesOf(columnValues.get(1));
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), dbHost, port);
            if (binlogFormat == null || !binlogFormat.isRow()) {
                isQuery = false;
                LOG.error("TaskName: " + getTaskName() + ", Unexcepted binlog format: " + binlogFormat.value);
            }

            Cat.logEvent("Slave.dbBinlogFormat", eventName, isQuery ? Message.SUCCESS : "1", "");
            if (isQuery) {
                LOG.info("TaskName: " + getTaskName() + ", Query config binlogformat is legal.");
            }
            return isQuery;
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", QueryConfig failed Reason: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send QueryCommand Packet to query binlog_format
     *
     * @return
     * @throws IOException
     */
    private boolean queryBinlogImage() throws IOException {
        try {
            QueryExecutor executor = new QueryExecutor(is, os);
            String cmd = "show variables like 'binlog_row_image'";
            ResultSet rs = executor.query(cmd, getContext());
            List<String> columnValues = rs.getFiledValues();
            boolean isQuery = true;
            if (columnValues == null || columnValues.size() == 0) {// 5.1
                isQuery = true;
            } else if (columnValues != null && columnValues.size() == 2 && columnValues.get(1) != null) {// 5.6
                BinlogRowImage binlogRowImage = BinlogRowImage.valuesOf(columnValues.get(1));
                isQuery = true;
                if (binlogRowImage == null || !binlogRowImage.isFull()) {
                    isQuery = false;
                    LOG.error("TaskName: " + getTaskName() + ", Unexcepted binlog row image: " + binlogRowImage.value);
                }
            } else {
                LOG.error("TaskName: " + getTaskName()
                        + ", QueryConfig failed Reason:unexcepted binlog row image query result.");
                isQuery = false;
            }
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), dbHost, port);
            Cat.logEvent("Slave.dbBinlogRowImage", eventName, isQuery ? Message.SUCCESS : "1", "");
            if (isQuery) {
                LOG.info("TaskName: " + getTaskName() + ", Query config binlog row image is legal.");
            }
            return isQuery;
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", QueryConfig failed Reason: " + e.getMessage());
            return false;
        }
    }

    protected void doStop() throws Exception {
        closeTransport();
    }

	private void stopTask() {
		String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), dbHost, port);
		try {
			// DefaultTaskContainer.instance.stopExecutor(this);
			Cat.logEvent("Slave.doStop", eventName, Message.SUCCESS, "");
		} catch (Exception e) {
			LOG.error("task " + getTaskName() + "stop error.");
			Cat.logEvent("Slave.doStop", eventName, "1", "");
		}
	}

    private void closeTransport() {
        // Close in.
        try {
            if (this.is != null) {
                this.is.close();
            }
        } catch (IOException ioEx) {
            LOG.warn("Server " + this.getTaskName() + ", Failed to close the input stream.");
        } finally {
            this.is = null;
        }

        // Close os.
        try {
            if (this.os != null) {
                this.os.close();
            }
        } catch (IOException ioEx) {
            LOG.warn("Server " + this.getTaskName() + ", Failed to close the output stream");
        } finally {
            this.os = null;
        }

		// Close socket.
		try {
			if (this.mysqlSocket != null) {
				this.mysqlSocket.close();
			}
		} catch (IOException ioEx) {
			LOG.warn("Server " + this.getTaskName() + ", Failed to close the socket", ioEx);
		} finally {
			this.mysqlSocket = null;
		}
	}

    public void initContext() {
        PumaContext context = new PumaContext();

        BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(this.getTaskName());

        if (binlogInfo == null) {
            context.setBinlogStartPos(this.getBinlogInfo().getBinlogPosition());
            context.setBinlogFileName(this.getBinlogInfo().getBinlogFile());
        } else {
            context.setBinlogFileName(binlogInfo.getBinlogFile());
            context.setBinlogStartPos(binlogInfo.getBinlogPosition());
        }

        // context.setPumaServerId(taskExecutor.getServerId());
        context.setPumaServerName(this.getTaskName());
        this.setContext(context);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDBHost() {
        return dbHost;
    }

    public void setDBHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDBUsername() {
        return dbUsername;
    }

    public void setDBUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDBPassword() {
        return dbPassword;
    }

    public void setDBPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return the database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @param database the database to set
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * @return the dbServerId
     */
    public long getDbServerId() {
        return dbServerId;
    }

    /**
     * @param dbServerId the dbServerId to set
     */
    public void setDbServerId(long dbServerId) {
        this.dbServerId = dbServerId;
    }

    public BinlogInfo getBinlogInfo() {
        return this.state.getBinlogInfo();
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.state.setBinlogInfo(binlogInfo);
    }

    public BinlogStat getBinlogStat() {
        return this.state.getBinlogStat();
    }

    public void setBinlogStat(BinlogStat binlogStat) {
        this.state.setBinlogStat(binlogStat);
    }

    public void incrRowsInsert() {
        Long rowsInsert = this.state.getBinlogStat().getRowsInsert();
        this.getBinlogStat().setRowsInsert(rowsInsert + 1);
    }

    public void incrRowsUpdate() {
        Long rowsUpdate = this.state.getBinlogStat().getRowsUpdate();
        this.state.getBinlogStat().setRowsUpdate(rowsUpdate + 1);
    }

    public void incrRowsDelete() {
        Long rowsDelete = this.state.getBinlogStat().getRowsDelete();
        this.state.getBinlogStat().setRowsDelete(rowsDelete + 1);
    }

    public void incrDdls() {
        Long ddls = this.state.getBinlogStat().getDdls();
        this.state.getBinlogStat().setDdls(ddls + 1);
    }

    public static enum BinlogFormat {
        STATEMENT("STATEMENT"), ROW("ROW"), MIXED("MIXED");

        private String value;

        private BinlogFormat(String value) {
            this.value = value;
        }

        public boolean isRow() {
            return this == ROW;
        }

        public boolean isStatement() {
            return this == STATEMENT;
        }

        public boolean isMixed() {
            return this == MIXED;
        }

        public static BinlogFormat valuesOf(String value) {
            BinlogFormat[] formats = values();
            for (BinlogFormat format : formats) {
                if (format.value.equalsIgnoreCase(value)) {
                    return format;
                }
            }
            return null;
        }
    }

    public static enum BinlogRowImage {

        FULL("FULL"), MINIMAL("MINIMAL"), NOBLOB("NOBLOB");

        private String value;

        private BinlogRowImage(String value) {
            this.value = value;
        }

        public boolean isFull() {
            return this == FULL;
        }

        public boolean isMinimal() {
            return this == MINIMAL;
        }

        public boolean isNOBLOB() {
            return this == NOBLOB;
        }

        public static BinlogRowImage valuesOf(String value) {
            BinlogRowImage[] images = values();
            for (BinlogRowImage image : images) {
                if (image.value.equalsIgnoreCase(value)) {
                    return image;
                }
            }
            return null;
        }
    }
}
