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
package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.MySQLCommunicationConstant;
import com.dianping.puma.parser.mysql.utils.MySQLUtils;
import com.dianping.puma.utils.PacketUtils;

import java.nio.ByteBuffer;

/**
 * 
 * @author Leo Liang
 * 
 */
public class ConnectPacket extends AbstractResponsePacket {

	private static final long	serialVersionUID	= -4346727912577548259L;

	@Override
	protected void doReadPacket(ByteBuffer buf, PumaContext context) {
		context.setProtocolVersion(buf.get());
		context.setServerVersion(PacketUtils.readNullTerminatedString(buf));
		parseVersion(context);

		if (MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
				context.getServerSubMinorVersion(), 4, 0, 8)) {
			context.setMaxThreeBytes((256 * 256 * 256) - 1);
		} else {
			context.setMaxThreeBytes(255 * 255 * 255);
		}
		if(versionProduct(context.getServerMajorVersion(), context.getServerMinorVersion(),
				context.getServerSubMinorVersion()) >= BinlogConstants.checksumVersionProduct){
			context.setChecksumAlg(BinlogConstants.CHECKSUM_ALG_CRC32);
		}
		context.setThreadId(PacketUtils.readLong(buf, 4));
		context.setSeed(PacketUtils.readNullTerminatedString(buf));

		context.setServerCapabilities(0);

		if (buf.position() < buf.limit()) {
			context.setServerCapabilities(PacketUtils.readInt(buf, 2));
		}

		if ((MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
				context.getServerSubMinorVersion(), 4, 1, 1) || ((context.getProtocolVersion() > 9) && (context
				.getServerCapabilities() & MySQLCommunicationConstant.CLIENT_PROTOCOL_41) != 0))) {

			/* New protocol with 16 bytes to describe server characteristics */
			context.setServerCharsetIndex(buf.get() & 0xff);
			context.setServerStatus(PacketUtils.readInt(buf, 2));

			// context.setServerCapabilities(context.getServerCapabilities() +
			// 65536 * PacketUtil.readInt(buf, 2));

			buf.position(buf.position() + 13);
			String seedPart2 = PacketUtils.readNullTerminatedString(buf);
			StringBuilder newSeed = new StringBuilder(20);
			newSeed.append(context.getSeed());
			newSeed.append(seedPart2);
			context.setSeed(newSeed.toString());
		}

		if (context.getProtocolVersion() > 9) {
			context.setClientParam(context.getClientParam() | MySQLCommunicationConstant.CLIENT_LONG_PASSWORD);
		} else {
			context.setClientParam(context.getClientParam() & ~MySQLCommunicationConstant.CLIENT_LONG_PASSWORD);
		}

		if ((MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
				context.getServerSubMinorVersion(), 4, 1, 0) || ((context.getProtocolVersion() > 9) && (context
				.getServerCapabilities() & MySQLCommunicationConstant.CLIENT_RESERVED) != 0))) {
			if ((MySQLUtils.versionMeetsMinimum(context.getServerMajorVersion(), context.getServerMinorVersion(),
					context.getServerSubMinorVersion(), 4, 1, 1) || ((context.getProtocolVersion() > 9) && (context
					.getServerCapabilities() & MySQLCommunicationConstant.CLIENT_PROTOCOL_41) != 0))) {
				context.setClientParam(context.getClientParam() | MySQLCommunicationConstant.CLIENT_PROTOCOL_41);
				context.setHas41NewNewProt(true);

			} else {
				context.setClientParam(context.getClientParam() | MySQLCommunicationConstant.CLIENT_RESERVED);
				context.setHas41NewNewProt(false);
			}

			context.setUse41Extensions(true);
		}

	}

	/**
     * 
     */
	private void parseVersion(PumaContext context) {
		// Parse the server version into major/minor/subminor
		int point = context.getServerVersion().indexOf('.');

		if (point != -1) {
			try {
				int n = Integer.parseInt(context.getServerVersion().substring(0, point));
				context.setServerMajorVersion(n);
			} catch (NumberFormatException nfe) {
				// ignore
			}

			String remaining = context.getServerVersion().substring(point + 1, context.getServerVersion().length());
			point = remaining.indexOf('.'); //$NON-NLS-1$

			if (point != -1) {
				try {
					int n = Integer.parseInt(remaining.substring(0, point));
					context.setServerMinorVersion(n);
				} catch (NumberFormatException nfe) {
					// ignore
				}

				remaining = remaining.substring(point + 1, remaining.length());

				int pos = 0;

				while (pos < remaining.length()) {
					if ((remaining.charAt(pos) < '0') || (remaining.charAt(pos) > '9')) {
						break;
					}

					pos++;
				}

				try {
					int n = Integer.parseInt(remaining.substring(0, pos));
					context.setServerSubMinorVersion(n);
				} catch (NumberFormatException nfe) {
					// ignore
				}
			}
		}
	}

	private long versionProduct(int serverMajorVersion,int serverMinorVersion,int serverSubMinorVersion) {
		return ((serverMajorVersion * 256 + serverMinorVersion) * 256 + serverSubMinorVersion);
	}

}
