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
package com.dianping.puma.taskexecutor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.datahandler.DataHandlerResult;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
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
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.zebra.util.JDBCUtils;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import jodd.util.collection.SortedArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

    private SrcDbEntity currentSrcDbEntity;

    private DefaultTableMetaInfoFetcher tableMetaInfoFetcher;

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
                loadServerId(getTask().getSrcDbEntityList());

                // 读position/file文件
                BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(getContext().getPumaServerName());

                if (binlogInfo == null) {
                    this.currentSrcDbEntity = initSrcDbByServerId(-1);
                    if (getTask().getBeginTime() != null) {
                        binlogInfo = getBinlogByTimestamp(getTask().getBeginTime().getTime() / 1000);
                    }
                } else {
                    this.currentSrcDbEntity = initSrcDbByServerId(binlogInfo.getServerId());

                    if (binlogInfo.getServerId() != currentSrcDbEntity.getServerId()) {
                        BinlogInfo oldBinlogInfo = binlogInfo;
                        binlogInfo = getBinlogByTimestamp(oldBinlogInfo.getTimestamp() - 60);
                        if (binlogInfo == null) {
                            throw new IOException("Switch Binlog Failed!");
                        } else {
                            Cat.logEvent("BinlogSwitch", getTask().getName(), Message.SUCCESS,
                                    oldBinlogInfo.toString() + " -> " + binlogInfo.toString());
                        }
                    }
                }

                updateTableMetaInfoFetcher();
                getContext().setMasterUrl(currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());

                if (!connect()) {
                    throw new IOException("Connection failed.");
                }

                canStop = true;

                initConnect();

                initBinlogPosition(binlogInfo);

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
                    this.currentSrcDbEntity = chooseNextSrcDb();
                    updateTableMetaInfoFetcher();
                    failCount = 0;
                }
                LOG.error("Exception occurs. taskName: " + getTaskName() + " dbServerId: " + (currentSrcDbEntity == null ? 0 : currentSrcDbEntity.getServerId())
                        + ". Reconnect...", e);

                Thread.sleep(((failCount % 10) + 1) * 2000);
            }
        } while (!isStop());

    }

    protected void initBinlogPosition(BinlogInfo binlogInfo) throws IOException {
        if (binlogInfo == null) {
            List<BinlogInfo> binaryLogs = getBinaryLogs();
            BinlogInfo begin = getTask().getBeginTime() == null ? binaryLogs.get(binaryLogs.size() - 1) : binaryLogs.get(0);
            binlogInfo = new BinlogInfo(currentSrcDbEntity.getServerId(), begin.getBinlogFile(), 4l, 0, begin.getTimestamp());
        }

        getContext().setDBServerId(currentSrcDbEntity.getServerId());
        getContext().setBinlogFileName(binlogInfo.getBinlogFile());
        getContext().setBinlogStartPos(binlogInfo.getBinlogPosition());
        setBinlogInfo(binlogInfo);
        SystemStatusManager.addServer(getTaskName(), currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort(), getTask().getTableSet());
        SystemStatusManager.updateServerBinlog(getTaskName(), binlogInfo);
    }

    protected void loadServerId(Collection<SrcDbEntity> srcDbEntityList) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        for (SrcDbEntity entity : srcDbEntityList) {

            Connection conn = null;
            Statement stmt = null;
            java.sql.ResultSet results = null;
            try {
                conn = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%d/", entity.getHost(), entity.getPort() == 0 ? 3306 : entity.getPort()),
                        entity.getUsername(),
                        entity.getPassword());

                stmt = conn.createStatement();
                results = stmt.executeQuery("show global variables like 'server_id'");
                results.next();
                entity.setServerId(results.getLong(2));
            } catch (Exception e) {
                Cat.logError(String.format("server id load failed: %s:%d", entity.getHost(), entity.getPort()), e);
                entity.setServerId(0);
            } finally {
                JDBCUtils.closeAll(results, stmt, conn);
            }
        }
    }

    protected void initConnect() throws IOException {
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

        if (queryServerId() != currentSrcDbEntity.getServerId()) {
            throw new IOException("Server Id Changed.");
        }
    }

    protected void updateTableMetaInfoFetcher() throws SQLException {
        tableMetaInfoFetcher.setSrcDbEntity(this.currentSrcDbEntity);
        tableMetaInfoFetcher.refreshTableMetas();
    }

    protected SrcDbEntity chooseNextSrcDb() {
        SrcDbEntity oldSrcEntity = this.currentSrcDbEntity;

        List<SrcDbEntity> sortedSet = new SortedArrayList<SrcDbEntity>(new Comparator<SrcDbEntity>() {
            @Override
            public int compare(SrcDbEntity o1, SrcDbEntity o2) {
                if (o1.getServerId() > o1.getServerId()) {
                    return 1;
                } else if (o1.getServerId() < o1.getServerId()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        sortedSet.addAll(getTask().getSrcDbEntityList());

        int index = sortedSet.indexOf(this.currentSrcDbEntity) + 1;
        if (index >= sortedSet.size()) {
            index = 0;
        }
        SrcDbEntity newSrcEntity = sortedSet.get(index);

        Cat.logEvent("SrcDbSwitch", getTask().getName(), Message.SUCCESS,
                oldSrcEntity.toString() + " -> " + newSrcEntity.toString());

        return newSrcEntity;
    }

    protected SrcDbEntity initSrcDbByServerId(final long binlogServerId) throws IOException {
        List<SrcDbEntity> avaliableSrcDb = FluentIterable
                .from(getTask().getSrcDbEntityList())
                .filter(new Predicate<SrcDbEntity>() {
                    @Override
                    public boolean apply(SrcDbEntity input) {
                        return input.getServerId() > 0;
                    }
                }).toList();

        if (avaliableSrcDb.size() == 0) {
            throw new IOException("No Avaliable SrcDB");
        }

        if (this.currentSrcDbEntity != null) {
            return this.currentSrcDbEntity;
        }

        SrcDbEntity srcDbEntity = Iterables.find(avaliableSrcDb, new Predicate<SrcDbEntity>() {
            @Override
            public boolean apply(SrcDbEntity input) {
                return input.getServerId() == binlogServerId;
            }
        }, null);

        if (srcDbEntity != null) {
            return srcDbEntity;
        }

        return srcDbEntity != null ? srcDbEntity : avaliableSrcDb.get(0);
    }

    protected BinlogInfo getBinlogByTimestamp(long time) throws IOException {
        if (!connect()) {
            throw new IOException("Connection failed.");
        }
        initConnect();
        List<BinlogInfo> binaryLogs = getBinaryLogs();
        BinlogInfo closestBinlogInfo = null;

        for (int k = binaryLogs.size() - 1; k >= 0; k--) {
            BinlogInfo newBinlogInfo = binaryLogs.get(k);

            getContext().setDBServerId(currentSrcDbEntity.getServerId());
            getContext().setBinlogFileName(newBinlogInfo.getBinlogFile());
            getContext().setBinlogStartPos(4);
            getContext().setMasterUrl(currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());

            if (!connect()) {
                throw new IOException("Connection failed.");
            }
            initConnect();

            if (dumpBinlog()) {
                while (!isStop()) {
                    BinlogPacket binlogPacket = (BinlogPacket) PacketFactory.parsePacket(is, PacketType.BINLOG_PACKET,
                            getContext());

                    if (!binlogPacket.isOk()) {
                        LOG.error("TaskName: " + getTaskName() + ", Binlog packet response error.");
                        throw new IOException("TaskName: " + getTaskName() + ", Binlog packet response error.");
                    } else {
                        BinlogEvent binlogEvent = parser.parse(binlogPacket.getBinlogBuf(), getContext());

                        try {
                            getContext().setNextBinlogPos(binlogEvent.getHeader().getNextPosition());

                            if (binlogEvent.getHeader().getEventType() == BinlogConstants.ROTATE_EVENT) {
                                if (closestBinlogInfo == null) {
                                    break;
                                } else {
                                    continue;
                                }
                            }

                            if (binlogEvent.getHeader().getEventType() != BinlogConstants.XID_EVENT) {
                                continue;
                            }

                            if (binlogEvent.getHeader().getTimestamp() >= time) {
                                if (closestBinlogInfo == null) {
                                    break;
                                } else {
                                    return closestBinlogInfo;
                                }
                            }

                            if (binlogEvent.getHeader().getTimestamp() <= time) {
                                closestBinlogInfo = new BinlogInfo(
                                        currentSrcDbEntity.getServerId(),
                                        getContext().getBinlogFileName(),
                                        binlogEvent.getHeader().getNextPosition(),
                                        0, binlogEvent.getHeader().getTimestamp());
                                continue;
                            }
                        } finally {
                            if (binlogEvent.getHeader().getEventType() == BinlogConstants.ROTATE_EVENT) {
                                RotateEvent rotateEvent = (RotateEvent) binlogEvent;
                                getContext().setBinlogFileName(rotateEvent.getNextBinlogFileName());
                                getContext().setBinlogStartPos(rotateEvent.getFirstEventPosition());
                            } else {
                                getContext().setBinlogStartPos(binlogEvent.getHeader().getNextPosition());
                            }
                        }
                    }
                }
            } else {
                throw new IOException("Binlog dump failed.");
            }
        }
        return null;
    }

    private void processBinlog() throws IOException {
        while (!isStop()) {
            // only slow down parsing, not stop
            if (SystemStatusManager.isStopTheWorld(this.getTaskName())) {
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

        SystemStatusManager.incServerParsedCounter(getTaskName());

        if (binlogEvent.getHeader().getEventType() == BinlogConstants.INTVAR_EVENT
                || binlogEvent.getHeader().getEventType() == BinlogConstants.RAND_EVENT
                || binlogEvent.getHeader().getEventType() == BinlogConstants.USER_VAR_EVENT) {
            LOG.error("TaskName: " + getTaskName() + ", Binlog_format is MIXED or STATEMENT ,System is not support.");
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());
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
            setBinlogInfo(new BinlogInfo(getBinlogInfo().getServerId(), getBinlogInfo().getBinlogFile(), binlogEvent
                    .getHeader().getNextPosition(), 0, 0));
        }

        if (binlogEvent.getHeader().getNextPosition() != 0
                && StringUtils.isNotBlank(getContext().getBinlogFileName())
                && dataHandlerResult != null
                && !dataHandlerResult.isEmpty()
                && (dataHandlerResult.getData() instanceof DdlEvent || (dataHandlerResult.getData() instanceof RowChangedEvent && ((RowChangedEvent) dataHandlerResult
                .getData()).isTransactionCommit()))) {

            BinlogInfo binlogInfo = new BinlogInfo(getContext().getDBServerId(), getContext()
                    .getBinlogFileName(), binlogEvent.getHeader().getNextPosition(), 0, binlogEvent.getHeader().getTimestamp());
            binlogInfoHolder.setBinlogInfo(getTaskName(), binlogInfo);
            SystemStatusManager.updateServerBinlog(getTaskName(), binlogInfo);
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
                    SystemStatusManager.incServerRowInsertCounter(getTaskName());
                    break;
                case RowChangedEvent.UPDATE:
                    incrRowsUpdate();
                    SystemStatusManager.incServerRowUpdateCounter(getTaskName());
                    break;
                case RowChangedEvent.DELETE:
                    incrRowsDelete();
                    SystemStatusManager.incServerRowDeleteCounter(getTaskName());
                    break;
                default:
                    break;
            }
        } else if (changedEvent instanceof DdlEvent) {
            incrDdls();
            SystemStatusManager.incServerDdlCounter(getTaskName());
        }
    }

    protected void processRotateEvent(BinlogEvent binlogEvent) {
        RotateEvent rotateEvent = (RotateEvent) binlogEvent;
        getContext().setBinlogFileName(rotateEvent.getNextBinlogFileName());
        getContext().setBinlogStartPos(rotateEvent.getFirstEventPosition());

        BinlogInfo binlogInfo = new BinlogInfo(getContext().getDBServerId(), rotateEvent.getNextBinlogFileName(),
                rotateEvent.getFirstEventPosition(), 0, rotateEvent.getHeader().getTimestamp());
        setBinlogInfo(binlogInfo);
        SystemStatusManager.updateServerBinlog(getTaskName(), binlogInfo);
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
            this.mysqlSocket.connect(new InetSocketAddress(currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort()));
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
                LOG.info("TaskName: " + getTaskName() + ", Dump binlog command success.");

                return true;
            } else {
                LOG.error("TaskName: " + getTaskName() + ", Dump binlog failed. Reason: "
                        + dumpCommandResultPacket.getMessage());

                return false;
            }
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + " Dump binlog failed. Reason: " + e.getMessage());

            return false;
        }

    }

    private List<BinlogInfo> getBinaryLogs() throws IOException {
        try {
            QueryExecutor executor = new QueryExecutor(is, os);
            String cmd = "SHOW BINARY LOGS";
            ResultSet rs = executor.query(cmd, getContext());
            List<String> values = rs.getFiledValues();
            List<BinlogInfo> result = new ArrayList<BinlogInfo>();
            for (int k = 0; k < values.size(); k += 2) {
                result.add(new BinlogInfo(currentSrcDbEntity.getServerId(), values.get(k), Long.valueOf(values.get(k + 1)), 0, 0));
            }
            return result;
        } catch (Exception e) {
            LOG.error("TaskName: " + getTaskName() + ", QueryConfig failed Reason: " + e.getMessage());
            throw new IOException(e);
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
            LOG.info("server logining taskName: " + getTaskName() + " host: " + currentSrcDbEntity.getHost() + " port: " + currentSrcDbEntity.getPort() + " username: "
                    + currentSrcDbEntity.getUsername() + " dbServerId: " + currentSrcDbEntity.getServerId());
            AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
                    PacketType.AUTHENTICATE_PACKET, getContext());

            authPacket.setPassword(currentSrcDbEntity.getPassword());
            authPacket.setUser(currentSrcDbEntity.getUsername());
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
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());
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


    private long queryServerId() throws IOException {
        try {
            QueryExecutor executor = new QueryExecutor(is, os);
            String cmd = "show global variables like 'server_id'";
            ResultSet rs = executor.query(cmd, getContext());
            List<String> columnValues = rs.getFiledValues();
            if (columnValues != null &&
                    columnValues.size() == 2 &&
                    !Strings.isNullOrEmpty(columnValues.get(1))) {
                return Long.valueOf(columnValues.get(1));
            }
            return -1;
        } catch (Exception e) {
            return -1;
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
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());
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
            String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());
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
        LOG.info("TaskName: " + getTaskName() + ", Stopped.");
        closeTransport();
    }

    private void stopTask() {
        String eventName = String.format("slave(%s) -- db(%s:%d)", getTaskName(), currentSrcDbEntity.getHost(), currentSrcDbEntity.getPort());
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

    public void setTableMetaInfoFetcher(DefaultTableMetaInfoFetcher tableMetaInfoFetcher) {
        this.tableMetaInfoFetcher = tableMetaInfoFetcher;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
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
