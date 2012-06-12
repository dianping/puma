/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-11 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server.mysql.packet;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.util.PacketUtil;
import com.dianping.puma.server.PumaContext;
import com.dianping.puma.server.mysql.MySQLConstant;
import com.dianping.puma.server.mysql.util.MySQLUtils;

/**
 * TODO Comment of AuthenticatePacket
 * 
 * @author Leo Liang
 * 
 */
public class AuthenticatePacket extends AbstractCommandPacket {
    /**
     * @param command
     */
    public AuthenticatePacket() {
        super((byte) 0xff);
    }

    private static final long serialVersionUID   = -5769286539834693024L;
    private static final int  UTF8_CHARSET_INDEX = 33;
    private String            user;
    private String            password;
    private String            seed;
    private String            database;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
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

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    @Override
    protected ByteBuffer doBuild(PumaContext context) throws IOException {

        int userLength = (user != null) ? user.length() : 0;
        int databaseLength = (database != null) ? database.length() : 0;
        ByteBuffer bodyBuf = ByteBuffer.allocate(((userLength + databaseLength) * 2) + 52);

        if ((context.getServerCapabilities() & MySQLConstant.CLIENT_SECURE_CONNECTION) != 0) {
            if (MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
                    context.getServerSubMinorVersion(), 4, 1, 1)) {
                secureAuth411(bodyBuf, user, password, database, true, context);
            } else {
                secureAuth411(bodyBuf, user, password, database, true, context);
            }
        } else {

            if (context.isUse41Extensions()) {
                PacketUtil.writeInt(bodyBuf, context.getClientParam(), 4);
                PacketUtil.writeInt(bodyBuf, context.getMaxThreeBytes(), 4);

                PacketUtil.writeByte(bodyBuf, (byte) 8);

                PacketUtil.writeBytesNoNull(bodyBuf, new byte[23]);
            } else {
                PacketUtil.writeInt(bodyBuf, (int) context.getClientParam(), 2);
                PacketUtil.writeInt(bodyBuf, context.getMaxThreeBytes(), 3);
            }

            // User/Password data
            PacketUtil.writeNullTerminatedString(bodyBuf, user, context.getEncoding());

            if (context.getProtocolVersion() > 9) {
                PacketUtil.writeNullTerminatedString(bodyBuf, MySQLUtils.newCrypt(password, context.getSeed()), context
                        .getEncoding());
            } else {
                PacketUtil.writeNullTerminatedString(bodyBuf, MySQLUtils.oldCrypt(password, context.getSeed()), context
                        .getEncoding());
            }

            if (((context.getServerCapabilities() & MySQLConstant.CLIENT_CONNECT_WITH_DB) != 0) && (database != null)
                    && (database.length() > 0)) {
                PacketUtil.writeNullTerminatedString(bodyBuf, database, context.getEncoding());
            }
        }
        return bodyBuf;
    }

    void secureAuth411(ByteBuffer buf, String user, String password, String database, boolean writeClientParams,
            PumaContext context) throws IOException {

        if (writeClientParams) {
            if (context.isUse41Extensions()) {
                if (MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
                        context.getServerSubMinorVersion(), 4, 1, 1)) {
                    PacketUtil.writeInt(buf, database != null && database.length() != 0 ? context.getClientParam()
                            | MySQLConstant.CLIENT_SECURE_CONNECTION | MySQLConstant.CLIENT_CONNECT_WITH_DB : context
                            .getClientParam()
                            | MySQLConstant.CLIENT_SECURE_CONNECTION, 4);
                    PacketUtil.writeInt(buf, context.getMaxThreeBytes(), 4);

                    PacketUtil.writeByte(buf, (byte) UTF8_CHARSET_INDEX);
                    PacketUtil.writeBytesNoNull(buf, new byte[23]);

                } else {
                    PacketUtil.writeInt(buf, context.getClientParam(), 4);
                    PacketUtil.writeInt(buf, context.getMaxThreeBytes(), 4);
                }
            } else {
                PacketUtil.writeInt(buf, (int) context.getClientParam(), 2);
                PacketUtil.writeInt(buf, context.getMaxThreeBytes(), 3);
            }
        }

        // User/Password data
        PacketUtil.writeNullTerminatedString(buf, user, context.getEncoding());

        if (password.length() != 0) {
            PacketUtil.writeByte(buf, (byte) 0x14);

            try {
                PacketUtil.writeBytesNoNull(buf, MySQLUtils.scramble411(password, this.seed, context.getEncoding()));
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        } else {
            /* For empty password */
            PacketUtil.writeByte(buf, (byte) 0);
        }

        if (database != null && database.length() > 0) {
            PacketUtil.writeNullTerminatedString(buf, database, context.getEncoding());
        } else {
            /* For empty database */
            PacketUtil.writeByte(buf, (byte) 0);
        }

    }
}
