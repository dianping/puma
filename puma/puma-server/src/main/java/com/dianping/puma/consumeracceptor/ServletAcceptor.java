/**
 * Project: puma-server
 * 
 * File Created at 2012-7-7
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.consumeracceptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;

/**
 * TODO Comment of ServletAcceptor
 * 
 * @author Leo Liang
 * 
 */
public class ServletAcceptor implements Acceptor {
	private static final Logger	log		= Logger.getLogger(ServletAcceptor.class);
	private Server				server;
	private int					port	= 7862;
	private EventStorage		storage;
	private String				name;

	/**
	 * @param storage
	 *            the storage to set
	 */
	public void setStorage(EventStorage storage) {
		this.storage = storage;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {
		server = new Server();
		Connector connector = new SelectChannelConnector();

		connector.setPort(port);
		connector.setMaxIdleTime(3000);
		server.setConnectors(new Connector[] { connector });
		Context context = new Context(server, "/", Context.SESSIONS);
		context.addServlet(new ServletHolder(new PumaAcceptorServlet()), "/puma/channel");
		server.setGracefulShutdown(1000);
		server.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {
		server.stop();
	}

	private class PumaAcceptorServlet extends HttpServlet {

		private static final long	serialVersionUID	= -8003860894193031060L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.
		 * HttpServletRequest, javax.servlet.http.HttpServletResponse)
		 */
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			doPost(req, resp);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.
		 * HttpServletRequest, javax.servlet.http.HttpServletResponse)
		 */
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			long seq = Long.parseLong(req.getParameter("seq"));
			boolean ddl = Boolean.valueOf(req.getParameter("ddl"));
			boolean dml = Boolean.valueOf(req.getParameter("dml"));
			boolean ts = Boolean.valueOf(req.getParameter("ts"));
			String codecType = req.getParameter("codec");
			String[] dts = req.getParameterValues("dt");
			String clientName = req.getParameter("name");

			EventCodec codec = EventCodecFactory.createCodec(codecType);

			EventChannel channel = storage.getChannel(seq);
			while (true) {
				try {
					ChangedEvent event = channel.next();
					EventFilterChain filterChain = EventFilterChainFactory.createEventFilterChain(ddl, dml, ts, dts);
					if (filterChain.doNext(event)) {
						byte[] data = codec.encode(event);
						resp.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
						resp.getOutputStream().write(data);
						resp.getOutputStream().flush();
					}
					Thread.sleep(2);
				} catch (Exception e) {
					log.info("One client disconnected. ClientName: " + clientName);
					break;
				}
			}
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAcceptorName() {
		return name;
	}

}
