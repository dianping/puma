<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page import="com.dianping.puma.channel.page.status.Model" %>
<%@ page import="com.dianping.puma.common.SystemStatusContainer.ServerStatus" %>
<%@ page import="com.dianping.puma.common.SystemStatusContainer.ClientStatus" %>
<%@ page import="com.dianping.puma.storage.Sequence" %>
<jsp:useBean id="ctx" type="com.dianping.puma.channel.page.status.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.puma.channel.page.status.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.puma.channel.page.status.Model" scope="request"/>

Servers
<table>
	<tr><th>name</th><th>host</th><th>port</th><th>db</th><th>binlog</th><th>pos</th></tr>
	<% 
		Map<String, ServerStatus> servers = model.getSystemStatus().listServerStatus(); 
		for(Map.Entry<String, ServerStatus> entry : servers.entrySet()) {
			ServerStatus ss = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + ss.getHost() + "</td><td>" + ss.getPort() + "</td><td>" + ss.getDb() + "</td><td>" + ss.getBinlogFile() + "</td><td>" + ss.getBinlogPos() + "</td></tr>" %>
	<%
		}
	%>
</table>
<br>

Storages
<table>
	<tr><th>name</th><th>seq</th></tr>
	<% 
		Map<String, Long> storages = model.getSystemStatus().listStorageStatus(); 
		for(Map.Entry<String, Long> entry : storages.entrySet()) {
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>"%>
            <%=entry.getValue() + "(" + new Sequence(entry.getValue()) + ")"%>
            <%="</td></tr>" %>
	<%
		}
	%>
</table>

<br>
Clients
<table>
	<tr><th>name</th><th>target</th><th>seq</th><th>db&table</th><th>codec</th><th>needDdl</th><th>needDml</th><th>needTransactionInfo</th></tr>
	<% 
		Map<String, ClientStatus> clients = model.getSystemStatus().listClientStatus(); 
		for(Map.Entry<String, ClientStatus> entry : clients.entrySet()) {
			ClientStatus cs = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + cs.getTarget() + "</td><td>" %>
            <%=cs.getSeq() + "(" + new Sequence(cs.getSeq()) + ")" %> 
            <%="</td><td>" %>
			<% for(String dt: cs.getDt()){
			%> 
				<%=dt %>
			<% } %>
			<%="</td><td>" + cs.getCodec() + "</td><td>" + cs.isDdl() + "</td><td>" + cs.isDml() + "</td><td>"+ "</td><td>" + cs.isNeedTsInfo() + "</td><td>"+ "</td></tr>" %>
	<%
		}
	%>
</table>