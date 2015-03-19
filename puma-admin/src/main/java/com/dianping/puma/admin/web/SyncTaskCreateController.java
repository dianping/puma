package com.dianping.puma.admin.web;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import com.dianping.puma.admin.reporter.SyncTaskOperationReporter;
import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.*;
import com.dianping.puma.core.service.PumaTaskService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.config.Config;
import com.dianping.puma.admin.monitor.SystemStatusContainer;
import com.dianping.puma.admin.service.DumpTaskService;
import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.service.SyncServerService;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.taskexecutor.TaskExecutorStatus;

/**
 * (1) 以create为整个controller，所有中间状态存放在session <br>
 * (2) 编写SyncTask的service <br>
 * (3) pumaSyncServer的id与host的映射 <br>
 * (4) 保存binlog信息，创建同步任务，启动任务
 * 
 * @author wukezhu
 */
@Controller
public class SyncTaskCreateController {
    private static final Logger LOG = LoggerFactory.getLogger(SyncTaskCreateController.class);
    //    private SyncConfigService syncConfigService;
    @Autowired
    private SrcDBInstanceService srcDBInstanceService;
    @Autowired
    private DstDBInstanceService dstDBInstanceService;
    @Autowired
    private DumpTaskService dumpTaskService;
    @Autowired
    private SyncServerService syncServerService;
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private com.dianping.puma.core.service.SyncTaskService syncTaskService2;
    @Autowired
    private com.dianping.puma.core.service.DumpTaskService dumpTaskService2;

    @Autowired
    private PumaTaskService pumaTaskService;
    @Autowired
    private SystemStatusContainer systemStatusContainer;

    @Autowired
    private SyncTaskOperationReporter syncTaskOperationReporter;

    @RequestMapping(value = { "/sync-task/create" })
    public ModelAndView create(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询MysqlConfig
        List<PumaTask> pumaTasks = pumaTaskService.findAll();
        List<DstDBInstance> dstDBInstances = dstDBInstanceService.findAll();

        //List<SrcDBInstance> srcDBInstances = srcDBInstanceService.findAll();
        //List<DstDBInstance> destDBInstances = dstDBInstanceService.findAll();
        //map.put("srcDBInstances", srcDBInstances);
        map.put("pumaTasks", pumaTasks);
        map.put("dstDBInstances", dstDBInstances);
        map.put("createActive", "active");
        map.put("path", "sync-task");
        map.put("subPath", "step1");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "/sync-task/create/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save(
          HttpSession session,
          String pumaTaskName,
          String dstDBInstanceName,
          String databaseFrom,
          String databaseTo,
          String[] tableFrom,
          String[] tableTo) {

        //清除之前的状态
    	LOG.info("create sync task step 1 save");
        session.removeAttribute("dumpMapping");
        session.removeAttribute("dumpTask");
        session.removeAttribute("srcDBInstance");
        session.removeAttribute("destDBInstance");
        session.removeAttribute("mysqlMapping");

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(pumaTaskName) || StringUtils.isBlank(dstDBInstanceName)) {
                throw new IllegalArgumentException("srcMysql和destMysql都不能为空。(srcMysql=" + pumaTaskName + ", destMysql=" + dstDBInstanceName
                        + ")");
            }
            if (tableFrom == null && tableTo == null) {
                tableFrom = new String[] { "*" };
                tableTo = new String[] { "*" };
            } else if (!(tableFrom != null && tableTo != null && tableFrom.length == tableTo.length)) {
                throw new IllegalArgumentException("源表个数和目标表的个数不一致。(tableFrom=" + tableFrom + ", tableTo=" + tableTo + ")");
            }
            //判断该srcMysql和destMysql是否重复
            /*
            if (this.syncTaskService.existsBySrcAndDest(srcMysql, destMysql)) {
                throw new IllegalArgumentException("创建失败，已有相同的配置存在。(srcMysqlName=" + srcMysql + ", destMysqlName=" + destMysql
                        + ")");
            }*/
            //解析mapping
            //有xml改为表格提交
            //            MysqlMapping mysqlMapping = SyncXmlParser.parse2(syncXml);
            MysqlMapping mysqlMapping = new MysqlMapping();
            DatabaseMapping database = new DatabaseMapping();
            database.setFrom(databaseFrom);
            database.setTo(databaseTo);
            for (int i = 0; i < tableFrom.length; i++) {
                String from = tableFrom[i];
                String to = tableTo[i];
                TableMapping table = new TableMapping();
                table.setFrom(from);
                table.setTo(to);
                database.addTable(table);
            }
            mysqlMapping.addDatabase(database);

            //保存到session
            session.setAttribute("pumaTaskName", pumaTaskName);
            session.setAttribute("dstDBInstanceName", dstDBInstanceName);
            session.setAttribute("mysqlMapping", mysqlMapping);
            //SrcDBInstance srcDBInstance = srcDBInstanceService.find(srcMysql);//.findByName();
            //DstDBInstance destDBInstance = dstDBInstanceService.find(destMysql);//.findByName(destMysql);
            //session.setAttribute("srcDBInstance", srcDBInstance);
            //session.setAttribute("destDBInstance", destDBInstance);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    @RequestMapping(method = RequestMethod.GET, value = { "/sync-task/create/step2" })
    public ModelAndView step2(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //从session拿出srcMysql，destMysql查询mysql配置
        //SrcDBInstance srcDBInstance = (SrcDBInstance) session.getAttribute("srcDBInstance");
        //DstDBInstance destDBInstance = (DstDBInstance) session.getAttribute("destDBInstance");
        //查询所有syncServer
        List<SyncServer> syncServers = syncServerService.findAll();
        //从会话中取出保存的mysqlMapping，计算出dumpMapping
        MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
        //MysqlHost mysqlHost = getSrcMysqlHost(srcDBInstance);
        DstDBInstance dstDBInstance = dstDBInstanceService.findByName((String) session.getAttribute("dstDBInstanceName"));
        MysqlHost mysqlHost = new MysqlHost();
        mysqlHost.setHost(dstDBInstance.getHost() + ":" + dstDBInstance.getPort());
        mysqlHost.setServerId(dstDBInstance.getServerId());
        mysqlHost.setUsername(dstDBInstance.getUsername());
        mysqlHost.setPassword(dstDBInstance.getPassword());

        DumpMapping dumpMapping = this.syncTaskService.convertMysqlMappingToDumpMapping(mysqlHost,
                mysqlMapping);
        session.setAttribute("dumpMapping", dumpMapping);

        //map.put("srcDBInstance", srcDBInstance);
        //map.put("destDBInstance", destDBInstance);
        map.put("syncServers", syncServers);
        map.put("dumpMapping", dumpMapping);
        map.put("createActive", "active");
        map.put("path", "sync-task");
        map.put("subPath", "step2");
        return new ModelAndView("main/container", map);
    }

    /**
     * 创建DumpTask
     */
    @RequestMapping(value = "/sync-task/create/createDumpTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createDumpTask(HttpSession session, String syncServerName) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //检查参数
            /*
            if (StringUtils.isBlank(srcMysqlHost)) {
                throw new IllegalArgumentException("srcMysqlHost不能为空");
            }
            if (StringUtils.isBlank(destMysqlHost)) {
                throw new IllegalArgumentException("destMysqlHost不能为空");
            }*/
            if (StringUtils.isBlank(syncServerName)) {
                throw new IllegalArgumentException("syncServerHost不能为空");
            }
            //从session拿出srcMysql，destMysql查询mysql配置
            //SrcDBInstance srcDBInstance = (SrcDBInstance) session.getAttribute("srcDBInstanceName");
            //DstDBInstance destDBInstance = (DstDBInstance) session.getAttribute("dstDBInstanceName");
            //DumpMapping dumpMapping = (DumpMapping) session.getAttribute("dumpMapping");
            //根据选择的srcMysqlHost，找出其MysqlHost对象
            //MysqlHost srcMysqlHost0 = getSrcMysqlHost(srcDBInstance);
            //根据选择的destMysqlHost，找出其MysqlHost对象
            //MysqlHost destMysqlHost0 = getDstMysqlHost(destDBInstance);

            com.dianping.puma.core.entity.DumpTask dumpTask = new com.dianping.puma.core.entity.DumpTask();

            String taskName = "hello";
            dumpTask.setName(taskName);
            dumpTask.setSyncType(SyncType.DUMP);
            dumpTask.setController(com.dianping.puma.core.constant.Controller.START);

            String pumaTaskName = (String) session.getAttribute("pumaTaskName");
            dumpTask.setPumaTaskName(pumaTaskName);

            String dstDBInstanceName = (String) session.getAttribute("dstDBInstanceName");
            dumpTask.setDstDBInstanceName(dstDBInstanceName);
            dumpTask.setSyncServerName(syncServerName);
            dumpTask.setDumpMapping((DumpMapping) session.getAttribute("dumpMapping"));

            dumpTaskService2.create(dumpTask);

            syncTaskOperationReporter.report(syncServerName, SyncType.DUMP, taskName, Operation.CREATE);

            /*
            //查询所有syncServer
            DumpTask dumpTask = new DumpTask();
            dumpTask.setSrcMysqlName(srcDBInstance.getName());
            dumpTask.setSrcMysqlHost(srcMysqlHost0);
            dumpTask.setDestMysqlName(destDBInstance.getName());
            dumpTask.setDestMysqlHost(destMysqlHost0);
            dumpTask.setDumpMapping(dumpMapping);
            dumpTask.setSyncServerName(syncServerName);
            //保存dumpTask到数据库
            dumpTaskService.create(dumpTask);*/
            //保存dumpTask到session
            session.setAttribute("dumpTask", dumpTask);
            LOG.info("created dumpTask: " + dumpTask);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    /**
     * 刷新DumpTask的状态
     */
    @RequestMapping(value = "/sync-task/create/refreshDumpStatus", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object refreshDumpStatus(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            DumpTask dumpTask = (DumpTask) session.getAttribute("dumpTask");
            //检查参数
            if (dumpTask == null) {
                throw new IllegalArgumentException("dumpTask为空，可能是会话已经过期！");
            }

            TaskExecutorStatus status = systemStatusContainer.getStatus(SyncType.DUMP, dumpTask.getName());
            if (status != null) {
                map.put("status", status);
                if (status.getBinlogInfo() != null) {
                    session.setAttribute("binlogInfo", status.getBinlogInfo());
                }
            }

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    @RequestMapping(method = RequestMethod.GET, value = { "/sync-task/create/step3" })
    public ModelAndView step3(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询所有syncServer
        List<SyncServer> syncServers = syncServerService.findAll();

        map.put("errorCodeHandlerMap", Config.getInstance().getErrorCodeHandlerMap());
        map.put("syncServers", syncServers);
        map.put("pumaClientName", "SyncTask-" + UUID.randomUUID());
        map.put("createActive", "active");
        map.put("path", "sync-task");
        map.put("subPath", "step3");
        return new ModelAndView("main/container", map);
    }

    /**
     * 创建SyncTask
     */
    @RequestMapping(value = "/sync-task/create/createSyncTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createSyncTask(HttpSession session, String syncServerName, String srcMysqlHost, String destMysqlHost,
                                 String binlogFile, String binlogPosition, Boolean ddl, Boolean dml, String pumaClientName,
                                 Boolean transaction, Integer[] errorCodes, String[] handlers, String defaultHandler) {
        Map<String, Object> map = new HashMap<String, Object>();
        /*
        try {
            //检查参数
            if (StringUtils.isBlank(syncServerName)) {
                throw new IllegalArgumentException("syncServerName不能为空");
            }
            if (StringUtils.isBlank(binlogFile)) {
                throw new IllegalArgumentException("binlogFile不能为空");
            }
            if (StringUtils.isBlank(binlogPosition)) {
                throw new IllegalArgumentException("binlogPosition不能为空");
            }
            if (StringUtils.isBlank(pumaClientName)) {
                throw new IllegalArgumentException("pumaClientName不能为空");
            }
            if ((errorCodes != null && handlers == null) || (errorCodes == null && handlers != null)
                    || (errorCodes != null && handlers != null && errorCodes.length != handlers.length)) {
                throw new IllegalArgumentException("errorCodes长度必须和handlers一致");
            }
            if (StringUtils.isBlank(defaultHandler)) {
            	throw new IllegalArgumentException("defaultHandler不能为空");
            }
            //从session拿出
            DumpTask dumpTask = (DumpTask) session.getAttribute("dumpTask");
            MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
            //创建SyncTask
            SyncTask syncTask = new SyncTask();
            if (dumpTask != null) {
                syncTask.setSrcMysqlName(dumpTask.getSrcMysqlName());
                syncTask.setSrcMysqlHost(dumpTask.getSrcMysqlHost());
                syncTask.setDestMysqlName(dumpTask.getDestMysqlName());
                syncTask.setDestMysqlHost(dumpTask.getDestMysqlHost());
                syncTask.setServerId(dumpTask.getSrcMysqlHost().getServerId());
            } else {
                //检查参数
                if (StringUtils.isBlank(destMysqlHost)) {
                    throw new IllegalArgumentException("destMysqlHost不能为空");
                }
                SrcDBInstance srcDBInstance = (SrcDBInstance) session.getAttribute("srcDBInstance");
                DstDBInstance destDBInstance = (DstDBInstance) session.getAttribute("destDBInstance");
                syncTask.setSrcDBInstanceId(srcDBInstance.getId());
                syncTask.setSrcMysqlName(srcDBInstance.getName());
                MysqlHost srcMysqlHost0 = getSrcMysqlHost(srcDBInstance);
                syncTask.setSrcMysqlHost(srcMysqlHost0);
                syncTask.setDestDBInstanceId(destDBInstance.getId());
                syncTask.setDestMysqlName(destDBInstance.getName());
                syncTask.setDestMysqlHost(getDstMysqlHost(destDBInstance));
                syncTask.setServerId(srcMysqlHost0.getServerId());
            }
            //解析errorCode,handler
            Map<Integer, String> errorCodeHandlerNames = new HashMap<Integer, String>();
            if (errorCodes != null) {
                for (int i = 0; i < errorCodes.length; i++) {
                    Integer errorCode = errorCodes[i];
                    String handler = handlers[i];
                    errorCodeHandlerNames.put(errorCode, handler);
                }
            }
            syncTask.setErrorCodeHandlerNameMap(errorCodeHandlerNames);
            syncTask.setDefaultHandler(StringUtils.trim(defaultHandler));
            syncTask.setMysqlMapping(mysqlMapping);
            syncTask.setSyncServerName(syncServerName);
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(binlogFile);
            binlogInfo.setBinlogPosition(Long.parseLong(binlogPosition));
            syncTask.setBinlogInfo(binlogInfo);
            syncTask.setPumaClientName(pumaClientName);
            syncTask.setDdl(ddl != null ? ddl : true);
            syncTask.setDml(dml != null ? dml : true);
            syncTask.setTransaction(transaction != null ? transaction : true);
            //保存dumpTask到数据库
            syncTaskService.create(syncTask);
            //更新dumpTask的syncTaskId
            Long syncTaskId = syncTask.getId();
            if (dumpTask != null) {
                long dumpTaskId = dumpTask.getId();
                this.dumpTaskService.updateSyncTaskId(dumpTaskId, syncTaskId);
            }
            LOG.info("created syncTask : " + syncTask);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);

    }

    private MysqlHost getMysqlHost(MysqlConfig mysqlConfig, String mysqlHostStr) {
        //根据选择的destMysqlHost，找出其MysqlHost对象
        MysqlHost mysqlHost = null;
        for (MysqlHost host : mysqlConfig.getHosts()) {
            if (host.getHost().equals(mysqlHostStr)) {
                mysqlHost = host;
                break;
            }
        }
        if (mysqlHost == null) {
            throw new IllegalArgumentException("destMysqlHost='" + mysqlHostStr + "' 不在数据库 " + mysqlConfig + " 中！");
        }
        return mysqlHost;
    }
    
    private MysqlHost getSrcMysqlHost(SrcDBInstance dbInstance){
    	MysqlHost mysqlHost = new MysqlHost();
    	mysqlHost.setHost(dbInstance.getHost() + ":"+dbInstance.getPort());
    	mysqlHost.setPassword(dbInstance.getPassword());
    	mysqlHost.setUsername(dbInstance.getUsername());
    	mysqlHost.setServerId(dbInstance.getServerId());
    	return mysqlHost;
    }
    
    private MysqlHost getDstMysqlHost(DstDBInstance dbInstance){
    	MysqlHost mysqlHost = new MysqlHost();
    	mysqlHost.setHost(dbInstance.getHost() + ":"+dbInstance.getPort());
    	mysqlHost.setPassword(dbInstance.getPassword());
    	mysqlHost.setUsername(dbInstance.getUsername());
    	mysqlHost.setServerId(dbInstance.getServerId());
    	return mysqlHost;
    }

}
