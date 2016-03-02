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
package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.MySQLCommunicationConstant;
import com.dianping.puma.parser.mysql.utils.MySQLUtils;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

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

	private static final long	serialVersionUID	= -5769286539834693024L;
	private static final int	UTF8_CHARSET_INDEX	= 33;
	private String				user;
	private String				password;
	private String				seed;
	private String				database;

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

		if ((context.getServerCapabilities() & MySQLCommunicationConstant.CLIENT_SECURE_CONNECTION) != 0) {
			context.setClientParam(context.getClientParam() | MySQLCommunicationConstant.CLIENT_SECURE_CONNECTION);
			if (MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
					context.getServerSubMinorVersion(), 4, 1, 1)) {
				secureAuth411(bodyBuf, user, password, database, true, context);
			} else {
				secureAuth411(bodyBuf, user, password, database, true, context);
			}
		} else {

			if (context.isUse41Extensions()) {
				PacketUtils.writeInt(bodyBuf, context.getClientParam(), 4);
				PacketUtils.writeInt(bodyBuf, context.getMaxThreeBytes(), 4);

				PacketUtils.writeByte(bodyBuf, (byte) 8);

				PacketUtils.writeBytesNoNull(bodyBuf, new byte[23]);
			} else {
				PacketUtils.writeInt(bodyBuf, context.getClientParam(), 2);
				PacketUtils.writeInt(bodyBuf, context.getMaxThreeBytes(), 3);
			}

			// User/Password data
			PacketUtils.writeNullTerminatedString(bodyBuf, user, context.getEncoding());

			if (context.getProtocolVersion() > 9) {
				PacketUtils.writeNullTerminatedString(bodyBuf, MySQLUtils.newCrypt(password, context.getSeed()),
						context.getEncoding());
			} else {
				PacketUtils.writeNullTerminatedString(bodyBuf, MySQLUtils.oldCrypt(password, context.getSeed()),
						context.getEncoding());
			}

			if (((context.getServerCapabilities() & MySQLCommunicationConstant.CLIENT_CONNECT_WITH_DB) != 0)
					&& (database != null) && (database.length() > 0)) {
				PacketUtils.writeNullTerminatedString(bodyBuf, database, context.getEncoding());
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
					PacketUtils.writeInt(buf, database != null && database.length() != 0 ? context.getClientParam()
							| MySQLCommunicationConstant.CLIENT_SECURE_CONNECTION
							| MySQLCommunicationConstant.CLIENT_CONNECT_WITH_DB : context.getClientParam()
							| MySQLCommunicationConstant.CLIENT_SECURE_CONNECTION, 4);
					PacketUtils.writeInt(buf, context.getMaxThreeBytes(), 4);

					PacketUtils.writeByte(buf, (byte) UTF8_CHARSET_INDEX);
					PacketUtils.writeBytesNoNull(buf, new byte[23]);

				} else {
					PacketUtils.writeInt(buf, context.getClientParam(), 4);
					PacketUtils.writeInt(buf, context.getMaxThreeBytes(), 4);
				}
			} else {
				PacketUtils.writeInt(buf, context.getClientParam(), 2);
				PacketUtils.writeInt(buf, context.getMaxThreeBytes(), 3);
			}
		}

		// User/Password data
		PacketUtils.writeNullTerminatedString(buf, user, context.getEncoding());

		if (password.length() != 0) {
			PacketUtils.writeByte(buf, (byte) 0x14);

			try {
				PacketUtils.writeBytesNoNull(buf, MySQLUtils.scramble411(password, this.seed, context.getEncoding()));
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}
		} else {
			/* For empty password */
			PacketUtils.writeByte(buf, (byte) 0);
		}

		if (database != null && database.length() > 0) {
			PacketUtils.writeNullTerminatedString(buf, database, context.getEncoding());
		} else {
			/* For empty database */
			PacketUtils.writeByte(buf, (byte) 0);
		}

	}
}
