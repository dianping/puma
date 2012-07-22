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
<style type="text/css"> 


body { 
font: normal 11px auto "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
color: #4f6b72; 
background: #E6EAE9; 
} 

a { 
color: #c75f3e; 
} 

.mytable { 
width: 700px; 
padding: 0; 
margin: 0; 
} 

caption { 
padding: 0 0 5px 0; 
width: 700px; 
font: italic 11px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
text-align: center; 
} 

th { 
font: bold 11px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
color: #4f6b72; 
border-right: 1px solid #C1DAD7; 
border-bottom: 1px solid #C1DAD7; 
border-top: 1px solid #C1DAD7; 
letter-spacing: 2px; 
text-transform: uppercase; 
text-align: left; 
padding: 6px 6px 6px 12px; 
background: #CAE8EA  no-repeat; 
} 

th.nobg { 
border-top: 0; 
border-left: 0; 
border-right: 1px solid #C1DAD7; 
background: none; 
} 

td { 
border-right: 1px solid #C1DAD7; 
border-bottom: 1px solid #C1DAD7; 
background: #fff; 
font-size:11px; 
padding: 6px 6px 6px 12px; 
color: #4f6b72; 
} 


td.alt { 
background: #F5FAFA; 
color: #797268; 
} 

th.spec { 
border-left: 1px solid #C1DAD7; 
border-top: 0; 
background: #fff no-repeat; 
font: bold 10px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
} 

th.specalt { 
border-left: 1px solid #C1DAD7; 
border-top: 0; 
background: #f5fafa no-repeat; 
font: bold 10px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
color: #797268; 
} 
/*---------for IE 5.x bug*/ 
html>body td{ font-size:11px;} 
body,td,th { 
font-family: 宋体, Arial; 
font-size: 12px; 
} 
</style> 
<table class="mytable">
<caption>Servers</caption>
	<tr><th>name</th><th>host</th><th>port</th><th>db</th><th>binlog</th><th>pos</th></tr>
	<% 
		Map<String, ServerStatus> servers = model.getSystemStatus().listServerStatus(); 
		for(Map.Entry<String, ServerStatus> entry : servers.entrySet()) {
			ServerStatus ss = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + ss.getHost() + "</td><td>" + ss.getPort() + "</td><td>" %>
			<% if(ss.getDb() == null || ss.getDb().length()==0){ %> All <% }else{ %> ss.getDb() <% } %> 
			<%="</td><td>" + ss.getBinlogFile() + "</td><td>" + ss.getBinlogPos() + "</td></tr>" %>
	<%
		}
	%>
</table>
<br>

<table class="mytable">
<caption>Storages</caption>
	<tr><th>name</th><th>seq</th></tr>
	<% 
		Map<String, Long> storages = model.getSystemStatus().listStorageStatus(); 
		for(Map.Entry<String, Long> entry : storages.entrySet()) {
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>"%>
            <%=entry.getValue() + "<br/>" + new Sequence(entry.getValue()) %>
            <%="</td></tr>" %>
	<%
		}
	%>
</table>

<br>
<table class="mytable">
<caption>Clients</caption>
	<tr><th>name</th><th>target</th><th>seq</th><th>db&table</th><th>codec</th><th>needDdl</th><th>needDml</th><th>needTransactionInfo</th></tr>
	<% 
		Map<String, ClientStatus> clients = model.getSystemStatus().listClientStatus(); 
		for(Map.Entry<String, ClientStatus> entry : clients.entrySet()) {
			ClientStatus cs = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + cs.getTarget() + "</td><td>" %>
            <%=cs.getSeq() + "<br/>" + new Sequence(cs.getSeq()) %> 
            <%="</td><td>" %>
			<% for(String dt: cs.getDt()){
			%> 
				<%=dt +"<br/>"%>
			<% } %>
			<%="</td><td>" + cs.getCodec() + "</td><td>" + cs.isDdl() + "</td><td>" + cs.isDml() + "</td><td>"+  cs.isNeedTsInfo() +"</td></tr>" %>
	<%
		}
	%>
</table>