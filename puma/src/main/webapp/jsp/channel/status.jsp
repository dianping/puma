<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.dianping.puma.channel.page.status.Model" %>
<%@ page import="com.dianping.puma.common.SystemStatusContainer.ClientStatus" %>
<%@ page import="com.dianping.puma.common.SystemStatusContainer.ServerStatus" %>
<%@ page import="com.dianping.puma.storage.Sequence" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.concurrent.atomic.AtomicLong" %>
<jsp:useBean id="ctx" type="com.dianping.puma.channel.page.status.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.puma.channel.page.status.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.puma.channel.page.status.Model" scope="request"/>

<style type="text/css"> 
.dotline {
BORDER-BOTTOM-STYLE: dotted; BORDER-LEFT-STYLE: dotted; BORDER-RIGHT-STYLE: dotted; BORDER-TOP-STYLE: dotted;
color: #4f6b72;

}

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
margin: 0 auto; 
} 

caption { 
padding: 0 0 5px 0; 
width: 700px; 
font: bold italic 15px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
margin: 0 auto; 
} 

th { 
font: bold  11px "Trebuchet MS", Verdana, Arial, Helvetica, sans-serif; 
color: #4f6b72; 
border-right: 1px solid #C1DAD7; 
border-bottom: 1px solid #C1DAD7; 
border-top: 1px solid #C1DAD7; 
letter-spacing: 2px; 
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

</style> 

<table class="mytable">
<caption>Servers</caption>
	<tr><th>Name</th><th>Host</th><th>Port</th><th>Db</th><th>Binlog</th><th>Pos</th><th>Pasred rows insert(since start)</th><th>Parsed rows delete(since start)</th><th>Parsed rows update(since start)</th><th>Parsed ddl events(since start)</th></tr>
	<% 
		Map<String, ServerStatus> servers = model.getSystemStatus().listServerStatus(); 
		Map<String, AtomicLong> updateCounter = model.getSystemStatus().listServerRowUpdateCounters();
		Map<String, AtomicLong> deleteCounter = model.getSystemStatus().listServerRowDeleteCounters();
		Map<String, AtomicLong> insertCounter = model.getSystemStatus().listServerRowInsertCounters();
		Map<String, AtomicLong> ddlCounter = model.getSystemStatus().listServerDdlCounters();
		for(Map.Entry<String, ServerStatus> entry : servers.entrySet()) {
			ServerStatus ss = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + ss.getHost() + "</td><td>" + ss.getPort() + "</td><td>" %>
			<% if(ss.getDb() == null || ss.getDb().length()==0){ %> All <% }else{ %> <%= ss.getDb() %> 
			<% } %> 
			<%="</td><td>" + ss.getBinlogFile() + "</td><td>" + ss.getBinlogPos() + "</td><td>" + (insertCounter.get(entry.getKey()) == null ? 0: insertCounter.get(entry.getKey()).longValue()) + "</td><td>" + (deleteCounter.get(entry.getKey()) == null ? 0: deleteCounter.get(entry.getKey()).longValue())  + "</td><td>" + (updateCounter.get(entry.getKey()) == null ? 0: updateCounter.get(entry.getKey()).longValue()) + "</td><td>" + (ddlCounter.get(entry.getKey()) == null ? 0: ddlCounter.get(entry.getKey()).longValue())+ "</td></tr>" %>
	<%
		}
	%>
</table>
<br>
<hr class="dotline" size=1>
<table class="mytable">
<caption>Storages</caption>
	<tr><th>Name</th><th>Seq</th></tr>
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
<hr class="dotline" size=1>
<br>
<table class="mytable">
<caption>Clients</caption>
	<tr><th>Name</th><th>Target</th><th>Seq</th><th>SuccessSeq</th><th>Db&table</th><th>codec</th><th>NeedDdl</th><th>NeedDml</th><th>NeedTransactionInfo</th></tr>
	<% 
		Map<String, ClientStatus> clients = model.getSystemStatus().listClientStatus();
		Map<String, Long> clientSussessSeq = model.getSystemStatus().listClientSuccessSeq();
		for(Map.Entry<String, ClientStatus> entry : clients.entrySet()) {
			ClientStatus cs = entry.getValue();
	%>	
			<%="<tr><td>" + entry.getKey() + "</td><td>" + cs.getTarget() + "</td><td>" %>
            <%=cs.getSeq() + "<br/>" + new Sequence(cs.getSeq()) %> 
            <%="</td><td>" %>
            <%if(clientSussessSeq.containsKey(entry.getKey())) {%>
            <%= clientSussessSeq.get(entry.getKey()) + "<br/>" + new Sequence(clientSussessSeq.get(entry.getKey())) %>
            <%}{ %>
            <%= ""%> <% }%>
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
<hr class="dotline" size=1>