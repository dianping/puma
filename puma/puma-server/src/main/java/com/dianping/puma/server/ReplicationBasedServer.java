/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-6 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.datahandler.DataHandlerResult;
import com.dianping.puma.parser.mysql.BinlogConstanst;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.RotateEvent;
import com.dianping.puma.parser.mysql.packet.AuthenticatePacket;
import com.dianping.puma.parser.mysql.packet.BinlogPacket;
import com.dianping.puma.parser.mysql.packet.ComBinlogDumpPacket;
import com.dianping.puma.parser.mysql.packet.OKErrorPacket;
import com.dianping.puma.parser.mysql.packet.PacketFactory;
import com.dianping.puma.parser.mysql.packet.PacketType;

/**
 * 基于MySQL复制机制的Server
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public class ReplicationBasedServer extends AbstractServer {

    private static final Logger log      = Logger.getLogger(ReplicationBasedServer.class);
    private int                 port     = 3306;
    private String              host;
    private String              user;
    private long                dbServerId;
    private String              password;
    private String              database;

    private String              encoding = "utf-8";
    private Socket              pumaSocket;
    private InputStream         is;
    private OutputStream        os;

    @Override
    public void doStart() throws Exception {

        long failCount = 0;
        do {
            try {
                // 读position/file文件
                PositionInfo posInfo = binlogPositionHolder.getPositionInfo(getContext().getPumaServerName(),
                        getContext().getBinlogFileName(), getContext().getBinlogStartPos());

                getContext().setBinlogFileName(posInfo.getBinlogFileName());
                getContext().setBinlogStartPos(posInfo.getBinlogPosition());
                getContext().setDBServerId(dbServerId);
                getContext().setMasterUrl(host, port);

                SystemStatusContainer.instance.updateServerStatus(getServerName(), host, port, database, getContext()
                        .getBinlogFileName(), getContext().getBinlogStartPos());

                connect();

                if (auth()) {
                    log.info("Server logined... serverId: " + getServerId() + " host: " + host + " port: " + port
                            + " user: " + user + " database: " + database + " dbServerId: " + getDbServerId());

                    if (dumpBinlog()) {
                        log.info("Dump binlog command success.");
                        processBinlog();
                    } else {
                        throw new IOException("Dump binlog failed.");
                    }
                } else {
                    throw new IOException("Login failed.");
                }
            } catch (Throwable e) {
                if (isStop()) {
                    return;
                }
                if (++failCount % 3 == 0) {
                    this.notifyService.alarm("[" + getContext().getPumaServerName() + "]" + "Failed to dump mysql["
                            + host + ":" + port + "] for 3 times.", e, true);
                    failCount = 0;
                }
                log.error("Exception occurs. serverId: " + getServerId() + " dbServerId: " + getDbServerId() + ". Reconnect...", e);
                Thread.sleep(((failCount % 10) + 1) * 2000);
            }
        } while (!isStop());

    }

    private void processBinlog() throws IOException {
        while (!isStop()) {
      	   if(SystemStatusContainer.instance.isStopTheWorld(this.getName())){
      	   	try{
      	   		TimeUnit.SECONDS.sleep(1);
      	   	}catch(InterruptedException e){
      	   		//ignore
      	   	}
      	   }

            BinlogPacket binlogPacket = (BinlogPacket) PacketFactory.parsePacket(is, PacketType.BINLOG_PACKET,
                    getContext());
            if (!binlogPacket.isOk()) {
                log.error("Binlog packet response error.");
                throw new IOException("Binlog packet response error.");
            } else {
                processBinlogPacket(binlogPacket);
            }

        }
    }

    protected void processBinlogPacket(BinlogPacket binlogPacket) throws IOException {
        BinlogEvent binlogEvent = parser.parse(binlogPacket.getBinlogBuf(), getContext());

        if (binlogEvent.getHeader().getEventType() != BinlogConstanst.FORMAT_DESCRIPTION_EVENT) {
            getContext().setNextBinlogPos(binlogEvent.getHeader().getNextPosition());
        }
        if (binlogEvent.getHeader().getEventType() == BinlogConstanst.ROTATE_EVENT) {
            processRotateEvent(binlogEvent);
        } else {
            processDataEvent(binlogEvent);
        }
    }

    protected void processDataEvent(BinlogEvent binlogEvent) {
        DataHandlerResult dataHandlerResult = null;
        // 一直处理一个binlogEvent的多行，处理完每行马上分发，以防止一个binlogEvent包含太多ChangedEvent而耗费太多内存
        do {
            dataHandlerResult = dataHandler.process(binlogEvent, getContext());
            if (dataHandlerResult != null && !dataHandlerResult.isEmpty()) {
                ChangedEvent changedEvent = dataHandlerResult.getData();

                updateOpsCounter(changedEvent);

                dispatch(changedEvent);
            }
        } while (dataHandlerResult != null && !dataHandlerResult.isFinished());

        if (binlogEvent.getHeader().getEventType() != BinlogConstanst.FORMAT_DESCRIPTION_EVENT) {
            getContext().setBinlogStartPos(binlogEvent.getHeader().getNextPosition());
        }
        // status report
        SystemStatusContainer.instance.updateServerStatus(getServerName(), host, port, database, getContext()
                .getBinlogFileName(), getContext().getBinlogStartPos());

        // 只有整个binlogEvent分发完了才save
        if (binlogEvent.getHeader() != null
                && binlogEvent.getHeader().getNextPosition() != 0
                && StringUtils.isNotBlank(getContext().getBinlogFileName())
                && dataHandlerResult != null
                && !dataHandlerResult.isEmpty()
                && (dataHandlerResult.getData() instanceof DdlEvent || (dataHandlerResult.getData() instanceof RowChangedEvent && ((RowChangedEvent) dataHandlerResult
                        .getData()).isTransactionCommit()))) {
            // save position
            binlogPositionHolder.savePositionInfo(getServerName(), new PositionInfo(binlogEvent.getHeader()
                    .getNextPosition(), getContext().getBinlogFileName()));
        }
    }

    protected void dispatch(ChangedEvent changedEvent) {
        try {
            dispatcher.dispatch(changedEvent, getContext());
        } catch (Exception e) {
            this.notifyService.alarm("[" + getContext().getPumaServerName() + "]" + "Dispatch event failed. event("
                    + changedEvent + ")", e, true);
            log.error("Dispatcher dispatch failed.", e);
        }
    }

    protected void updateOpsCounter(ChangedEvent changedEvent) {
        // 增加行变更计数器(除去ddl事件和事务信息事件)
        if ((changedEvent instanceof RowChangedEvent) && !((RowChangedEvent) changedEvent).isTransactionBegin()
                && !((RowChangedEvent) changedEvent).isTransactionCommit()) {
            switch (((RowChangedEvent) changedEvent).getActionType()) {
                case RowChangedEvent.INSERT:
                    SystemStatusContainer.instance.incServerRowInsertCounter(getServerName());
                    break;
                case RowChangedEvent.UPDATE:
                    SystemStatusContainer.instance.incServerRowUpdateCounter(getServerName());
                    break;
                case RowChangedEvent.DELETE:
                    SystemStatusContainer.instance.incServerRowDeleteCounter(getServerName());
                    break;
                default:
                    break;
            }
        } else if (changedEvent instanceof DdlEvent) {
            SystemStatusContainer.instance.incServerDdlCounter(getServerName());
        }
    }

    protected void processRotateEvent(BinlogEvent binlogEvent) {
        RotateEvent rotateEvent = (RotateEvent) binlogEvent;
        binlogPositionHolder.savePositionInfo(getServerName(), new PositionInfo(rotateEvent.getFirstEventPosition(),
                rotateEvent.getNextBinlogFileName()));
        getContext().setBinlogFileName(rotateEvent.getNextBinlogFileName());
        getContext().setBinlogStartPos(rotateEvent.getFirstEventPosition());
        // status report
        SystemStatusContainer.instance.updateServerStatus(getServerName(), host, port, database, getContext()
                .getBinlogFileName(), getContext().getBinlogStartPos());
    }

    /**
     * Connect to mysql master and parse the greeting packet
     * 
     * @throws IOException
     */
    private void connect() throws IOException {
        closeTransport();
        this.pumaSocket = new Socket();
        this.pumaSocket.setTcpNoDelay(false);
        this.pumaSocket.setKeepAlive(true);
        this.pumaSocket.connect(new InetSocketAddress(host, port));
        is = new BufferedInputStream(pumaSocket.getInputStream());
        os = new BufferedOutputStream(pumaSocket.getOutputStream());
        PacketFactory.parsePacket(is, PacketType.CONNECT_PACKET, getContext());
    }

    /**
     * Send COM_BINLOG_DUMP packet to mysql master and parse the response
     * 
     * @return
     * @throws IOException
     */
    private boolean dumpBinlog() throws IOException {
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
            if (StringUtils.isBlank(getContext().getBinlogFileName())
                    && StringUtils.isNotBlank(dumpCommandResultPacket.getMessage())) {
                String msg = dumpCommandResultPacket.getMessage();
                int startPos = msg.lastIndexOf(' ');
                if (startPos != -1) {
                    startPos += 1;
                } else {
                    startPos = 0;
                }
                String binlogFile = dumpCommandResultPacket.getMessage().substring(startPos);
                binlogPositionHolder.savePositionInfo(getServerName(), new PositionInfo(getContext()
                        .getBinlogStartPos(), binlogFile));
                getContext().setBinlogFileName(binlogFile);
            }
            return true;
        } else {
            log.error("Dump binlog failed. Reason: " + dumpCommandResultPacket.getMessage());
            return false;
        }
    }

    /**
     * Send Authentication Packet to mysql master and parse the response
     * 
     * @return
     * @throws IOException
     */
    private boolean auth() throws IOException {
        // auth
        AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
                PacketType.AUTHENTICATE_PACKET, getContext());

        authPacket.setPassword(password);
        authPacket.setUser(user);
        authPacket.setDatabase(database);
        authPacket.buildPacket(getContext());
        authPacket.write(os, getContext());

        OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(is, PacketType.OKERROR_PACKET,
                getContext());

        if (okErrorPacket.isOk()) {
            return true;
        } else {
            log.error("Login failed. Reason: " + okErrorPacket.getMessage());
            return false;
        }
    }

    protected void doStop() throws Exception {
        closeTransport();
    }

    private void closeTransport() {
        try {
            if (this.is != null) {
                this.is.close();
            }
        } catch (IOException ioEx) {
            log.warn("Server " + this.getServerName() + " failed to close the inputstream.");
        } finally {
            this.is = null;
        }
        try {
            if (this.os != null) {
                this.os.close();
            }
        } catch (IOException ioEx) {
            log.warn("Server " + this.getServerName() + " failed to close the outputstream");
        } finally {
            this.os = null;
        }
        try {
            if (this.pumaSocket != null) {
                this.pumaSocket.close();
            }
        } catch (IOException ioEx) {
            log.warn("Server " + this.getServerName() + " failed to close the socket", ioEx);
        } finally {
            this.pumaSocket = null;
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
     * @param database
     *            the database to set
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

}
