package com.dianping.puma.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public class AddressUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddressUtils.class);

    private static final String LOCALHOST_IP = "127.0.0.1";

    private static final String EMPTY_IP = "0.0.0.0";

    private static final Pattern IP_PATTERN = Pattern.compile("[0-9]{1,3}(\\.[0-9]{1,3}){3,}");

    public static boolean isAvailablePort(int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.bind(null);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static boolean isValidHostAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String name = address.getHostAddress();
        return (name != null && !EMPTY_IP.equals(name) && !LOCALHOST_IP.equals(name) && IP_PATTERN.matcher(name)
                .matches());
    }

    public static String getHostIp() {
        InetAddress address = getHostAddress();
        return address == null ? null : address.getHostAddress();
    }

    public static String getHostName() {
        InetAddress address = getHostAddress();
        return address == null ? null : address.getHostName();
    }

    /**
     * 判断该ip是否为本机ip，一台机器可能同时有多个IP
     *
     * @param ip
     * @return
     */
    public static boolean isHostIp(String ip) {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidHostAddress(localAddress)
                    && (localAddress.getHostAddress().equals(ip) || localAddress.getHostName().equals(ip))) {
                return true;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving local host ip address, try scan network card ip address. cause: "
                    + e.getMessage());
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidHostAddress(address) && address.getHostAddress().equals(ip)) {
                                        return true;
                                    }
                                } catch (Throwable e) {
                                    LOGGER.warn("Failed to retriving network card ip address. cause:" + e.getMessage());
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to retriving network card ip address. cause:" + e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving network card ip address. cause:" + e.getMessage());
        }

        return false;
    }

    public static InetAddress getHostAddress() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidHostAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving local host ip address, try scan network card ip address. cause: "
                    + e.getMessage());
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidHostAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    LOGGER.warn("Failed to retriving network card ip address. cause:" + e.getMessage());
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to retriving network card ip address. cause:" + e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving network card ip address. cause:" + e.getMessage());
        }
        LOGGER.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }
}
