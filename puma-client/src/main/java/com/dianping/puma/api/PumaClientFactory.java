package com.dianping.puma.api;

import com.dianping.puma.api.impl.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class PumaClientFactory {

	/**
	 * 获取支持连接单个Puma Server的Puma Client。
	 * 
	 * @param clientName
	 *           自定义一个puma-client的名字
	 * @param serverIpAddress
	 *           Puma Server的IP地址
	 * @return
	 */
	public static PumaClient createSimplePumaClient(String clientName, String serverIpAddress) {
		return new SimplePumaClient(getAppName() + "." + clientName, serverIpAddress);
	}

	/**
	 * 获取支持多个连接Puma Server的Puma Client,具有HA的功能。 所连接的Puma Server服务器地址会根据订阅的数据库自动获得。
	 * 
	 * @param clientName
	 *           自定义一个puma-client的名字
	 * @return
	 */
	public static PumaClient createClusterPumaClient(String clientName) {
		ZookeeperPumaServerMonitor monitor = new ZookeeperPumaServerMonitor();
		MonitorBasedPumaServerRouter router = new MonitorBasedPumaServerRouter(monitor);

		return new ClusterPumaClient(getAppName() + "." + clientName, router);
	}

	/**
	 * 获取支持多个连接Puma Server的Puma Client,具有HA的功能。
	 * 
	 * @param clientName
	 *           自定义一个puma-client的名字
	 * @param serverIpAddress
	 *           Puma Server的IP地址
	 * @return
	 */
	public static PumaClient createClusterPumaClient(String clientName, List<String> serverIpAddress) {
		ConstantPumaServerMonitor monitor = new ConstantPumaServerMonitor(serverIpAddress);
		MonitorBasedPumaServerRouter router = new MonitorBasedPumaServerRouter(monitor);

		return new ClusterPumaClient(getAppName() + "." + clientName, router);
	}

	private static String getAppName() {
		URL appProperties = PumaClientFactory.class.getResource("/META-INF/app.properties");

		if (appProperties != null) {
			InputStream in = null;
			try {
				in = appProperties.openStream();
				Properties properties = new Properties();
				properties.load(in);

				String appName = (String) properties.get("app.name");

				if (appName != null) {
					return appName;
				} else {
					return "noname";
				}
			} catch (IOException ignore) {
				return "noname";
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						// ignore it
					}
				}
			}
		} else {
			return "noname";
		}
	}
}
