/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-7 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server.mysql.packet;

import java.nio.ByteBuffer;

import com.dianping.puma.common.util.PacketUtil;

/**
 * 
 * @author Leo Liang
 * 
 */
public class HandshakePacket extends AbstractResponsePacket {

    private static final long serialVersionUID = -4346727912577548259L;
    private byte              protocolVersion;
    private String            serverVersion;
    private long              threadId;
    private String            seed;
    private int               serverCapabilities;
    private int               serverCharsetIndex;
    private int               serverStatus;


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.puma.server.mysql.packet.AbstractPacket#doReadPacket(java
     * .nio.ByteBuffer)
     */
    @Override
    protected void doReadPacket(ByteBuffer buf) {
        this.protocolVersion = buf.get();
        this.serverVersion = PacketUtil.readNullTerminatedString(buf);
        this.threadId = PacketUtil.readLong(buf, 4);
        this.seed = PacketUtil.readNullTerminatedString(buf);

        this.serverCapabilities = 0;

        if (buf.position() < buf.limit()) {
            this.serverCapabilities = PacketUtil.readInt(buf, 2);
        }
        this.serverCharsetIndex = buf.get() & 0xff;
        this.serverStatus = PacketUtil.readInt(buf, 2);
        buf.position(buf.position() + 16);
        String seedPart2 = PacketUtil.readNullTerminatedString(buf);
        StringBuilder newSeed = new StringBuilder(20);
        newSeed.append(this.seed);
        newSeed.append(seedPart2);
        this.seed = newSeed.toString();
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public long getThreadId() {
        return threadId;
    }

    public String getSeed() {
        return seed;
    }

    public int getServerCapabilities() {
        return serverCapabilities;
    }

    public int getServerCharsetIndex() {
        return serverCharsetIndex;
    }

    public int getServerStatus() {
        return serverStatus;
    }

    @Override
    public String toString() {
        return "HandshakePacket [protocolVersion=" + protocolVersion + ", seed=" + seed + ", serverCapabilities="
                + serverCapabilities + ", serverCharsetIndex=" + serverCharsetIndex + ", serverStatus=" + serverStatus
                + ", serverVersion=" + serverVersion + ", threadId=" + threadId + "]";
    }

}
