<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page import="com.dianping.puma.channel.page.status.Model" %>
<%@ page import="com.dianping.puma.common.SystemStatusContainer.ServerStatus" %>
<jsp:useBean id="ctx" type="com.dianping.puma.channel.page.status.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.puma.channel.page.status.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.puma.channel.page.status.Model" scope="request"/>

Servers
<table>
	<tr><th>name</th><th>host</th><th>port</th><th>db</th><th>binlog</th><th>pos</th></tr>
	<% 
		Map<String, ServerStatus> map = model.getSystemStatus().listServerStatus(); 
		for(Map.Entry<String, ServerStatus> entry : map.entrySet()) {
			ServerStatus ss = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + ss.getHost() + "</td><td>" + ss.getPort() + "</td><td>" + ss.getDb() + "</td><td>" + ss.getBinlogFile() + "</td><td>" + ss.getBinlogPos() + "</td></tr>" %>
	<%
		}
	%>
</table>