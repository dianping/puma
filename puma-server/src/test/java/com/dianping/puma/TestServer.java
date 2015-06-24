//package com.dianping.puma;
//
//import java.io.File;
//import java.net.URL;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import org.apache.jasper.servlet.JspServlet;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.servlet.Context;
//import org.mortbay.jetty.servlet.ServletHolder;
//
//import com.dianping.puma.servlet.PumaListener;
//import org.unidal.test.browser.Browser;
//import org.unidal.test.browser.DefaultBrowser;
//import org.unidal.web.MVC;
//
//public class TestServer {
//	private static TestServer	s_instance;
//
//	private static Context		s_ctx;
//
//	private Server				m_server;
//
//	@AfterClass
//	public static void afterClass() throws Exception {
//		if (s_instance != null) {
//			s_instance.shutdown();
//			s_instance = null;
//		}
//	}
//
//	@BeforeClass
//	public static void beforeClass() throws Exception {
//		s_instance = new TestServer();
//		s_instance.configure();
//		s_instance.startServer(s_ctx);
//		s_instance.postConfigure(s_ctx);
//	}
//
//	public static void main(String[] args) throws Exception {
//		TestServer.beforeClass();
//
//		try {
//			s_instance.before();
//			s_instance.test();
//			s_instance.after();
//		} finally {
//			TestServer.afterClass();
//		}
//	}
//
//	@After
//	public void after() {
//	}
//
//	@Before
//	public void before() {
//	}
//
//	protected void configure() throws Exception {
//		Context ctx = new Context(Context.SESSIONS);
//		String contextPath = getContextPath();
//
//		if (contextPath != null) {
//			if (contextPath.length() == 0) {
//				contextPath = null;
//			} else if (!contextPath.startsWith("/")) {
//				throw new RuntimeException(String.format("ContextPath(%s) must be null or starting with '/'.",
//						contextPath));
//			}
//		}
//
//		ctx.setResourceBase(getWarRoot().getPath());
//		ctx.setContextPath(contextPath == null ? "/" : contextPath);
//		ctx.addEventListener(new PumaListener());
//
//		configureJsp(ctx);
//		s_ctx = ctx;
//	}
//
//	protected ServletHolder configureJsp(Context ctx) throws Exception {
//		ServletHolder jsp = ctx.addServlet(JspServlet.class, "*.jsp");
//		String scratchDir = getScratchDir().getCanonicalPath();
//
//		if (scratchDir != null) {
//			jsp.setInitParameter("scratchdir", scratchDir);
//		}
//
//		jsp.setInitParameter("keepgenerated", "true");
//		jsp.setInitParameter("genStringAsCharArray", "true");
//		return jsp;
//	}
//
//	protected void display(String requestUri) throws Exception {
//		StringBuilder sb = new StringBuilder(256);
//		Browser browser = new DefaultBrowser();
//
//		sb.append("http://localhost:").append(getServerPort()).append(requestUri);
//		browser.display(new URL(sb.toString()));
//	}
//
//	protected String getContextPath() {
//		return "/puma";
//	}
//
//	protected File getScratchDir() {
//		File work = new File(System.getProperty("java.io.tmpdir", "."), "Puma");
//
//		work.mkdirs();
//		return work;
//	}
//
//	protected int getServerPort() {
//		return 7862;
//	}
//
//	protected String getTimestamp() {
//		return new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());
//	}
//
//	protected File getWarRoot() {
//		return new File("src/main/webapp");
//	}
//
//	protected void postConfigure(Context ctx) {
//		ServletHolder mvc = new ServletHolder(new MVC());
//
//		mvc.setInitParameter("cat-client-xml", "/data/appdatas/cat/client.xml");
//		ctx.addServlet(mvc, "/channel/*");
//	}
//
//	protected void shutdown() throws Exception {
//		if (m_server != null) {
//			m_server.stop();
//		}
//	}
//
//	protected void startServer(Context ctx) throws Exception {
//		Server server = new Server(getServerPort());
//
//		server.setStopAtShutdown(true);
//		server.setHandler(ctx);
//		server.start();
//
//		m_server = server;
//	}
//
//	@Test
//	public void test() throws Exception {
//		// open the page in the default browser
//		display("/puma/channel/status");
//
//		System.out.println(String.format("[%s] [INFO] Press any key to stop server ... ", getTimestamp()));
//		System.in.read();
//	}
//}
