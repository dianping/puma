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

import com.dianping.puma.server.Server;
import com.dianping.puma.server.mysql.packet.PacketFactory;
import com.dianping.puma.server.mysql.packet.PacketType;

/**
 *基于MySQL复制机制的Server
 * 
 * @author Leo Liang
 * 
 */
public class ReplicationBasedServer implements Server {
    protected int    port           = 3306;
    protected String host;
    protected String user;
    protected String password;
    protected int    serverId       = 6789;
    protected String binlogFileName;
    protected long   binlogPosition = 4;
    protected String encoding       = "utf-8";

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.server.Server#start()
     */
    @Override
    public void start() throws Exception {
        Socket s = new Socket(host, port);
        System.out.println(PacketFactory.parsePacket(s.getInputStream(), PacketType.HANDSHAKE_PACKET));
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

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
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
        rbs.start();
    }

}
