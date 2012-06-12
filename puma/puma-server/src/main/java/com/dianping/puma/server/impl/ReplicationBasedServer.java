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
package com.dianping.puma.server.impl;

import java.net.Socket;

import org.apache.log4j.Logger;

import com.dianping.puma.server.PumaContext;
import com.dianping.puma.server.Server;
import com.dianping.puma.server.mysql.packet.AuthenticatePacket;
import com.dianping.puma.server.mysql.packet.ComBinlogDumpPacket;
import com.dianping.puma.server.mysql.packet.OKErrorPacket;
import com.dianping.puma.server.mysql.packet.PacketFactory;
import com.dianping.puma.server.mysql.packet.PacketType;

/**
 *基于MySQL复制机制的Server
 * 
 * @author Leo Liang
 * 
 */
public class ReplicationBasedServer implements Server {
    private static final Logger log            = Logger.getLogger(ReplicationBasedServer.class);
    protected int               port           = 3306;
    protected String            host;
    protected String            user;
    protected String            password;
    protected String            database;
    protected long              serverId       = 6789;
    protected String            binlogFileName;
    protected long              binlogPosition = 4;
    protected String            encoding       = "utf-8";

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.server.Server#start()
     */
    @Override
    public void start() throws Exception {
        Socket s = new Socket(host, port);
        PumaContext context = new PumaContext();
        context.setBinlogFileName(binlogFileName);
        context.setBinlogStartPos(binlogPosition);
        context.setServerId(serverId);
        // connect
        PacketFactory.parsePacket(s.getInputStream(), PacketType.CONNECT_PACKET, context);

        // auth
        AuthenticatePacket authPacket = (AuthenticatePacket) PacketFactory.createCommandPacket(
                PacketType.AUTHENTICATE_PACKET, context);

        authPacket.setPassword(password);
        authPacket.setUser(user);
        authPacket.setDatabase(database);
        authPacket.buildPacket(context);
        authPacket.write(s.getOutputStream(), context);

        OKErrorPacket okErrorPacket = (OKErrorPacket) PacketFactory.parsePacket(s.getInputStream(),
                PacketType.OKERROR_PACKET, context);

        if (okErrorPacket.isOk()) {
            log.info("Logined...");

            ComBinlogDumpPacket dumpBinlogPacket = (ComBinlogDumpPacket) PacketFactory.createCommandPacket(
                    PacketType.COM_BINLOG_DUMP_PACKET, context);
            dumpBinlogPacket.setBinlogFileName(binlogFileName);
            dumpBinlogPacket.setBinlogFlag(0);
            dumpBinlogPacket.setBinlogPosition(binlogPosition);
            dumpBinlogPacket.setServerId(serverId);
            dumpBinlogPacket.buildPacket(context);

            dumpBinlogPacket.write(s.getOutputStream(), context);

            OKErrorPacket dumpCommandResultPacket = (OKErrorPacket) PacketFactory.parsePacket(s.getInputStream(),
                    PacketType.OKERROR_PACKET, context);

            if (dumpCommandResultPacket.isOk()) {
                log.info("Dump binlog command success.");
            } else {
                log.error("Dump binlog failed. Reason: " + dumpCommandResultPacket.getMessage());
            }

        } else {
            log.error("Login failed. Reason: " + okErrorPacket.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.server.Server#stop()
     */
    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub

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

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getBinlogFileName() {
        return binlogFileName;
    }

    public void setBinlogFileName(String binlogFileName) {
        this.binlogFileName = binlogFileName;
    }

    public long getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public static void main(String[] args) throws Exception {
        ReplicationBasedServer rbs = new ReplicationBasedServer();
        rbs.setHost("192.168.7.43");
        rbs.setPort(3306);
        rbs.setUser("binlog");
        rbs.setPassword("binlog");
        rbs.setBinlogFileName("mysql-bin.000006");
        rbs.setBinlogPosition(4);
        rbs.start();
    }

}
