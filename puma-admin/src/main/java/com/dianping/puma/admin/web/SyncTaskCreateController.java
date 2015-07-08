//package com.dianping.puma.admin.web;
//
//import com.dianping.puma.admin.config.Config;
//import com.dianping.puma.admin.remote.SyncTaskControllerReporter;
//import com.dianping.puma.admin.remote.SyncTaskOperationReporter;
//import com.dianping.puma.admin.util.GsonUtil;
//import com.dianping.puma.admin.util.MysqlMetaInfoFetcher;
//import com.dianping.puma.biz.entity.DumpTask;
//import com.dianping.puma.biz.entity.PumaTask;
//import com.dianping.puma.biz.entity.SrcDBInstance;
//import com.dianping.puma.biz.entity.SyncTask;
//import com.dianping.puma.biz.service.*;
//import com.dianping.puma.biz.sync.model.config.MysqlHost;
//import com.dianping.puma.biz.sync.model.mapping.*;
//import com.dianping.puma.core.constant.ActionOperation;
//import com.dianping.puma.core.constant.Status;
//import com.dianping.puma.core.constant.SyncType;
//import com.dianping.puma.core.model.BinlogInfo;
//import com.dianping.puma.core.model.state.BaseSyncTaskState;
//import com.dianping.puma.core.model.state.DumpTaskState;
//import com.dianping.puma.core.model.state.TaskStateContainer;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpSession;
//import java.sql.SQLException;
//import java.util.*;
//
///**
// * (1) 以create为整个controller，所有中间状态存放在session <br>
// * (2) 编写SyncTask的service <br>
// * (3) pumaSyncServer的id与host的映射 <br>
// * (4) 保存binlog信息，创建同步任务，启动任务
// *
// * @author wukezhu
// */
//@Controller
//public class SyncTaskCreateController {
//
//    private static final Logger LOG = LoggerFactory.getLogger(SyncTaskCreateController.class);
//
//    @Autowired
//    private SrcDBInstanceService srcDBInstanceService;
//
//    @Autowired
//    private DstDBInstanceService dstDBInstanceService;
//
//    @Autowired
//    private SyncServerService syncServerService;
//
//    @Autowired
//    private SyncTaskService syncTaskService;
//
//    @Autowired
//    private DumpTaskService dumpTaskService;
//
//    @Autowired
//    private PumaTaskService pumaTaskService;
//
//    @Autowired
//    private SyncTaskOperationReporter syncTaskOperationReporter;
//
//    @Autowired
//    private SyncTaskControllerReporter syncTaskControllerReporter;
//
//    @Autowired
//    TaskStateContainer syncTaskStateContainer;
//
//    @Autowired
//    SyncTaskStateService syncTaskStateService;
//
//    @Autowired
//    DumpTaskStateService dumpTaskStateService;
//
//    //@RequestMapping(value = "/sync-task/create/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public Object step1Save(HttpSession session, String pumaTaskName, String dstDBInstanceName, String syncServerName,
//                            String databaseFrom, String databaseTo, String[] tableFrom, String[] tableTo) {
//
//        // 清除之前的状态
//        LOG.info("create sync task step 1 save");
//        session.removeAttribute("dumpMapping");
//        session.removeAttribute("dumpTask");
//        session.removeAttribute("pumaTaskName");
//        session.removeAttribute("dstDBInstanceName");
//        session.removeAttribute("mysqlMapping");
//        session.removeAttribute("syncServerName");
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            if (StringUtils.isBlank(pumaTaskName) || StringUtils.isBlank(dstDBInstanceName)) {
//                throw new IllegalArgumentException("srcMysql和destMysql都不能为空。(srcMysql=" + pumaTaskName + ", destMysql="
//                        + dstDBInstanceName + ")");
//            }
//            if (tableFrom == null && tableTo == null) {
//                tableFrom = new String[]{"*"};
//                tableTo = new String[]{"*"};
//            } else if (!(tableFrom != null && tableTo != null && tableFrom.length == tableTo.length)) {
//                throw new IllegalArgumentException("源表个数和目标表的个数不一致。(tableFrom=" + tableFrom + ", tableTo=" + tableTo
//                        + ")");
//            }
//            // 判断该srcMysql和destMysql是否重复
//            /*
//             * if (this.syncTaskService.existsBySrcAndDest(srcMysql, destMysql))
//			 * { throw new
//			 * IllegalArgumentException("创建失败，已有相同的配置存在。(srcMysqlName=" +
//			 * srcMysql + ", destMysqlName=" + destMysql + ")"); }
//			 */
//            // 解析mapping
//            // 有xml改为表格提交
//            // MysqlMapping mysqlMapping = SyncXmlParser.parse2(syncXml);
//            MysqlMapping mysqlMapping = new MysqlMapping();
//            DatabaseMapping database = new DatabaseMapping();
//            database.setFrom(databaseFrom);
//            database.setTo(databaseTo);
//            for (int i = 0; i < tableFrom.length; i++) {
//                String from = tableFrom[i];
//                String to = tableTo[i];
//                TableMapping table = new TableMapping();
//                table.setFrom(from);
//                table.setTo(to);
//                database.addTable(table);
//            }
//            mysqlMapping.addDatabase(database);
//
//            // 保存到session
//            session.setAttribute("pumaTaskName", pumaTaskName);
//            session.setAttribute("dstDBInstanceName", dstDBInstanceName);
//            session.setAttribute("syncServerName", syncServerName);
//            session.setAttribute("mysqlMapping", mysqlMapping);
//
//            map.put("success", true);
//        } catch (IllegalArgumentException e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//        } catch (Exception e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//            LOG.error(e.getMessage(), e);
//        }
//        return GsonUtil.toJson(map);
//
//    }
//
//    //@RequestMapping(method = RequestMethod.GET, value = {"/sync-task/create/step2"})
//    public ModelAndView step2(HttpSession session) throws SQLException {
//        Map<String, Object> map = new HashMap<String, Object>();
//        DumpTask dumpTask = (DumpTask) session.getAttribute("dumpTask");
//        if (dumpTask == null) {
//            // 从会话中取出保存的mysqlMapping，计算出dumpMapping
//            MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
//            // MysqlHost mysqlHost = getSrcMysqlHost(srcDBInstance);
//            String pumaTaskName = (String) session.getAttribute("pumaTaskName");
//            String dstDBInstanceName = (String) session.getAttribute("dstDBInstanceName");
//            PumaTask pumaTask = pumaTaskService.find(pumaTaskName);
//
//            SrcDBInstance srcDBInstance = srcDBInstanceService.find(pumaTask.getSrcDBInstanceName());
//            MysqlHost mysqlHost = new MysqlHost();
//            mysqlHost.setHost(srcDBInstance.getHost() + ":" + srcDBInstance.getPort());
//            mysqlHost.setServerId(srcDBInstance.getServerId());
//            mysqlHost.setUsername(srcDBInstance.getUsername());
//            mysqlHost.setPassword(srcDBInstance.getPassword());
//
//            DumpMapping dumpMapping = this.convertMysqlMappingToDumpMapping(mysqlHost, mysqlMapping);
//            session.setAttribute("dumpMapping", dumpMapping);
//            map.put("dumpTaskName", "DumpTask-" + pumaTaskName + "@" + dstDBInstanceName);
//            map.put("dumpMapping", dumpMapping);
//        } else {
//            map.put("dumpTaskName", dumpTask.getName());
//            map.put("dumpMapping", dumpTask.getDumpMapping());
//        }
//        map.put("createActive", "active");
//        map.put("path", "sync-task");
//        map.put("subPath", "step2");
//        return new ModelAndView("main/container", map);
//    }
//
//    /**
//     * 创建DumpTask
//     */
//    //@RequestMapping(value = "/sync-task/create/createDumpTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public Object createDumpTask(HttpSession session, String dumpTaskName) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            // 检查参数
//
//            DumpTask dumpTask = new DumpTask();
//            String syncServerName = (String) session.getAttribute("syncServerName");
//            dumpTask.setName(dumpTaskName);
//            dumpTask.setSyncType(SyncType.DUMP);
//            dumpTask.setController(com.dianping.puma.core.constant.ActionController.START);
//
//            String pumaTaskName = (String) session.getAttribute("pumaTaskName");
//            dumpTask.setPumaTaskName(pumaTaskName);
//
//            String dstDBInstanceName = (String) session.getAttribute("dstDBInstanceName");
//            dumpTask.setDstDBInstanceName(dstDBInstanceName);
//            dumpTask.setSyncServerName(syncServerName);
//            dumpTask.setDumpMapping((DumpMapping) session.getAttribute("dumpMapping"));
//
//            dumpTaskService.create(dumpTask);
//
//            //dumpTaskOperationReporter.report(syncServerName, dumpTaskName, ActionOperation.CREATE);
//
//            // 保存dumpTask到session
//            session.setAttribute("dumpTask", dumpTask);
//            LOG.info("created dumpTask: " + dumpTask);
//
//            map.put("success", true);
//        } catch (IllegalArgumentException e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//        } catch (Exception e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//            LOG.error(e.getMessage(), e);
//        }
//        return GsonUtil.toJson(map);
//
//    }
//
//    /**
//     * 刷新DumpTask的状态
//     */
//    //@RequestMapping(value = "/sync-task/create/refreshDumpStatus", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public Object refreshDumpStatus(HttpSession session) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            DumpTask dumpTask = (DumpTask) session.getAttribute("dumpTask");
//            // 检查参数
//            if (dumpTask == null) {
//                throw new IllegalArgumentException("dumpTask为空，可能是会话已经过期！");
//            }
//
//            DumpTaskState dumpTaskState = dumpTaskStateService.find(dumpTask.getName());
//            if (dumpTaskState != null) {
//                map.put("state", dumpTaskState);
//                if (dumpTaskState.getBinlogInfo() != null) {
//                    session.setAttribute("binlogInfo", dumpTaskState.getBinlogInfo());
//                }
//            }
//
//            map.put("success", true);
//        } catch (IllegalArgumentException e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//        } catch (Exception e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//            LOG.error(e.getMessage(), e);
//        }
//        return GsonUtil.toJson(map);
//
//    }
//
//    //@RequestMapping(method = RequestMethod.GET, value = {"/sync-task/create/step3"})
//    public ModelAndView step3(HttpSession session) throws SQLException {
//        Map<String, Object> map = new HashMap<String, Object>();
//        // 查询所有syncServer
//        String pumaTaskName = (String) session.getAttribute("pumaTaskName");
//        String dstDBInstanceName = (String) session.getAttribute("dstDBInstanceName");
//
//        map.put("binlogInfo", session.getAttribute("binlogInfo"));
//        session.removeAttribute("binlogInfo");
//
//        map.put("errorCodeHandlerMap", Config.getInstance().getErrorCodeHandlerMap());
//
//        map.put("pumaClientName", "SyncTask-" + pumaTaskName + "@" + dstDBInstanceName);
//        map.put("createActive", "active");
//        map.put("path", "sync-task");
//        map.put("subPath", "step3");
//        return new ModelAndView("main/container", map);
//    }
//
//    /**
//     * 创建SyncTask
//     */
//    //@RequestMapping(value = "/sync-task/create/createSyncTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public Object createSyncTask(HttpSession session, String binlogFile, String binlogPosition, Boolean ddl,
//                                 Boolean dml, String pumaClientName, Boolean transaction, Integer[] errorCodes, String[] handlers,
//                                 String defaultHandler) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        try {
//            // 检查参数
//            if (StringUtils.isBlank(binlogFile)) {
//                throw new IllegalArgumentException("binlogFile不能为空");
//            }
//            if (StringUtils.isBlank(binlogPosition)) {
//                throw new IllegalArgumentException("binlogPosition不能为空");
//            }
//            if (StringUtils.isBlank(pumaClientName)) {
//                throw new IllegalArgumentException("pumaClientName不能为空");
//            }
//            if ((errorCodes != null && handlers == null) || (errorCodes == null && handlers != null)
//                    || (errorCodes != null && handlers != null && errorCodes.length != handlers.length)) {
//                throw new IllegalArgumentException("errorCodes长度必须和handlers一致");
//            }
//            if (StringUtils.isBlank(defaultHandler)) {
//                throw new IllegalArgumentException("defaultHandler不能为空");
//            }
//            // 从session拿出
//            MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
//            String pumaTaskName = (String) session.getAttribute("pumaTaskName");
//            String dstDBInstanceName = (String) session.getAttribute("dstDBInstanceName");
//            String syncServerName = (String) session.getAttribute("syncServerName");
//
//            // 创建SyncTask
//            SyncTask syncTask = new SyncTask();
//            syncTask.setName(pumaClientName);
//            syncTask.setSyncType(SyncType.SYNC);
//            syncTask.setPumaTaskName(pumaTaskName);
//            syncTask.setDstDBInstanceName(dstDBInstanceName);
//            // 解析errorCode,handler
//            Map<Integer, String> errorCodeHandlerNames = new HashMap<Integer, String>();
//            if (errorCodes != null) {
//                for (int i = 0; i < errorCodes.length; i++) {
//                    Integer errorCode = errorCodes[i];
//                    String handler = handlers[i];
//                    errorCodeHandlerNames.put(errorCode, handler);
//                }
//            }
//            syncTask.setController(com.dianping.puma.core.constant.ActionController.START);
//            syncTask.setErrorCodeHandlerNameMap(errorCodeHandlerNames);
//            syncTask.setDefaultHandler(StringUtils.trim(defaultHandler));
//            syncTask.setMysqlMapping(mysqlMapping);
//            syncTask.setSyncServerName(syncServerName);
//            BinlogInfo binlogInfo = new BinlogInfo();
//            binlogInfo.setBinlogFile(binlogFile);
//            binlogInfo.setBinlogPosition(Long.parseLong(binlogPosition));
//            syncTask.setBinlogInfo(binlogInfo);
//            syncTask.setPumaClientName(pumaClientName);
//            syncTask.setDdl(ddl != null ? ddl : true);
//            syncTask.setDml(dml != null ? dml : true);
//            syncTask.setTransaction(transaction != null ? transaction : true);
//            // 保存dumpTask到数据库
//            syncTaskService.create(syncTask);
//            // 更新dumpTask的syncTaskId
//            /*Long syncTaskId = syncTask.getId();
//			if (dumpTask != null) {
//				long dumpTaskId = dumpTask.getId();
//				this.dumpTaskService.updateSyncTaskId(dumpTaskId, syncTaskId);
//			}*/
//
//            // Add to status container.
//            BaseSyncTaskState state = new BaseSyncTaskState();
//            state.setStatus(Status.WAITING);
//            syncTaskStateContainer.add(syncTask.getName(), state);
//
//            syncTaskOperationReporter
//                    .report(syncTask.getSyncServerName(), syncTask.getName(), ActionOperation.CREATE);
//
//            LOG.info("created syncTask : " + syncTask);
//
//            map.put("success", true);
//        } catch (IllegalArgumentException e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//        } catch (Exception e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//            LOG.error(e.getMessage(), e);
//        }
//        return GsonUtil.toJson(map);
//
//    }
//
//    private DumpMapping convertMysqlMappingToDumpMapping(MysqlHost mysqlHost, MysqlMapping mysqlMapping)
//            throws SQLException {
//        DumpMapping dumpMapping = new DumpMapping();
//        // dumpDatabaseMappings
//        // 遍历SyncConfig的DatabaseMapping，支持db和table名称改变，字段名称不支持改变。
//        List<DatabaseMapping> databaseMappings = mysqlMapping.getDatabases();
//        List<DatabaseMapping> dumpDatabaseMappings = new ArrayList<DatabaseMapping>();
//        dumpMapping.setDatabaseMappings(dumpDatabaseMappings);
//        for (DatabaseMapping databaseMapping : databaseMappings) {
//            String databaseConfigFrom = databaseMapping.getFrom();
//            String databaseConfigTo = databaseMapping.getTo();
//            List<TableMapping> dumpTableConfigs = new ArrayList<TableMapping>();
//            // 遍历table配置
//            List<TableMapping> tableConfigs = databaseMapping.getTables();
//            for (TableMapping tableConfig : tableConfigs) {
//                String tableConfigFrom = tableConfig.getFrom();
//                String tableConfigTo = tableConfig.getTo();
//                // 如果是from=*,to=*，则需要从数据库获取实际的表（排除已经列出的table配置）
//                if (StringUtils.equals(tableConfigFrom, "*") && StringUtils.equals(tableConfigTo, "*")) {
//                    // 访问数据库，得到该数据库下的所有表名(*配置是在最后的，所以排除已经列出的table配置就是排除dumpTableConfigs)
//                    MysqlMetaInfoFetcher mysqlExecutor = new MysqlMetaInfoFetcher(mysqlHost.getHost(), mysqlHost
//                            .getUsername(), mysqlHost.getPassword());
//                    List<String> tableNames;
//                    try {
//                        tableNames = mysqlExecutor.getTables(databaseConfigFrom);
//                    } finally {
//                        mysqlExecutor.close();
//                    }
//                    getRidOf(tableNames, dumpTableConfigs);
//                    for (String tableName : tableNames) {
//                        TableMapping dumpTableConfig = new TableMapping();
//                        dumpTableConfig.setFrom(tableName);
//                        dumpTableConfig.setTo(tableName);
//                        dumpTableConfigs.add(dumpTableConfig);
//                    }
//                } else {// 如果“table下的字段没有被重命名,partOf为false”，那么该table可以被dump
//                    if (shouldDump(tableConfig)) {
//                        TableMapping dumpTableConfig = new TableMapping();
//                        dumpTableConfig.setFrom(tableConfig.getFrom());
//                        dumpTableConfig.setTo(tableConfig.getTo());
//                        dumpTableConfigs.add(dumpTableConfig);
//                    }
//                }
//            }
//            // database需要dump(如果下属table没有需要dump则该database也不需要)
//            if (dumpTableConfigs.size() > 0) {
//                DatabaseMapping dumpDatabaseMapping = new DatabaseMapping();
//                dumpDatabaseMapping.setFrom(databaseConfigFrom);
//                dumpDatabaseMapping.setTo(databaseConfigTo);
//                dumpDatabaseMapping.setTables(dumpTableConfigs);
//                dumpDatabaseMappings.add(dumpDatabaseMapping);
//            }
//        }
//
//        return dumpMapping;
//    }
//
//    /**
//     * 从tableNames中去掉已经存在dumpTableConfigs(以TableConfig.getFrom()判断)中的表名
//     */
//    private void getRidOf(List<String> tableNames, List<TableMapping> dumpTableConfigs) {
//        Collection<String> dumpTableNames = new ArrayList<String>();
//        for (TableMapping tableConfig : dumpTableConfigs) {
//            dumpTableNames.add(tableConfig.getFrom());
//        }
//        tableNames.removeAll(dumpTableNames);
//    }
//
//    /**
//     * 如果“table下的字段没有被重命名,partOf为false”，那么该table可以被dump
//     */
//    private boolean shouldDump(TableMapping tableConfig) {
//        if (tableConfig.isPartOf()) {
//            return false;
//        }
//        List<ColumnMapping> columnConfigs = tableConfig.getColumns();
//        for (ColumnMapping columnConfig : columnConfigs) {
//            if (!StringUtils.equalsIgnoreCase(columnConfig.getFrom(), columnConfig.getTo())) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//
//}
