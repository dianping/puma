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

import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * TODO Comment of PacketFactory
 * 
 * @author Leo Liang
 * 
 */
public class PacketFactory {

    private static final Logger log = Logger.getLogger(PacketFactory.class);

    public static ResponsePacket parsePacket(InputStream is, PacketType packetType) {
        try {
            switch (packetType) {
                case HANDSHAKE_PACKET:
                    ResponsePacket greetingPacket = new HandshakePacket();
                    greetingPacket.readPacket(is);
                    return greetingPacket;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Read packet failed.", e);
            return null;
        }
    }
    
    public static CommandPacket createCommandPacket(PacketType packetType) {
        try {
            switch (packetType) {

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Read packet failed.", e);
            return null;
        }
    }

}
