package com.dianping.puma.admin.web;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXParseException;

import com.dianping.puma.admin.service.DumpActionService;
import com.dianping.puma.admin.service.MysqlConfigService;
import com.dianping.puma.admin.service.PumaSyncServerConfigService;
import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.admin.util.SyncXmlParser;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;

/**
 * TODO <br>
 * (1) 以create为整个controller，所有中间状态存放在session <br>
 * (2) 编写SyncTask的service <br>
 * (3) pumaSyncServer的id与host的映射 <br>
 * (4) 保存binlog信息，创建同步任务，启动任务
 * 
 * @author wukezhu
 */
@Controller
public class CreateController {
    private static final Logger LOG = LoggerFactory.getLogger(CreateController.class);
    //    private SyncConfigService syncConfigService;
    @Autowired
    private MysqlConfigService mysqlConfigService;
    @Autowired
    private DumpActionService dumpActionService;
    @Autowired
    private PumaSyncServerConfigService pumaSyncServerConfigService;
    @Autowired
    private SyncTaskActionService syncTaskActionService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";

    @RequestMapping(value = { "/create" })
    public ModelAndView create(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询MysqlConfig
        List<MysqlConfig> mysqlConfigs = mysqlConfigService.findAll();

        map.put("mysqlConfigs", mysqlConfigs);
        map.put("createActive", "active");
        map.put("path", "create");
        map.put("subPath", "step1");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "/create/step1Save_backup", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save_backup(HttpSession session, String srcMysql, String destMysql, String syncXml) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //判断该srcMysql和destMysql是否重复
            if (this.syncTaskActionService.existsBySrcAndDest(srcMysql, destMysql)) {
                throw new IllegalArgumentException("创建失败，已有相同的配置存在。(srcMysqlName=" + srcMysql + ", destMysqlName=" + destMysql
                        + ")");
            }
            //保存到session
            MysqlMapping mysqlMapping = SyncXmlParser.parse2(syncXml);
            session.setAttribute("mysqlMapping", mysqlMapping);
            MysqlConfig srcMysqlConfig = mysqlConfigService.find(srcMysql);
            MysqlConfig destMysqlConfig = mysqlConfigService.find(destMysql);
            session.setAttribute("srcMysqlConfig", srcMysqlConfig);
            session.setAttribute("destMysqlConfig", destMysqlConfig);

            map.put("success", true);
        } catch (SAXParseException e) {
            map.put("success", false);
            map.put("errorMsg", "xml解析出错：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    @RequestMapping(value = "/create/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save(HttpSession session, String srcMysql, String destMysql, String databaseFrom, String databaseTo,
                            String[] tableFrom, String[] tableTo) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(srcMysql) || StringUtils.isBlank(destMysql)) {
                throw new IllegalArgumentException("srcMysql和destMysql都不能为空。(srcMysql=" + srcMysql + ", destMysql=" + destMysql
                        + ")");
            }
            if (tableFrom == null && tableTo == null) {
                tableFrom = new String[] { "*" };
                tableTo = new String[] { "*" };
            } else if (!(tableFrom != null && tableTo != null && tableFrom.length == tableTo.length)) {
                throw new IllegalArgumentException("源表个数和目标表的个数不一致。(tableFrom=" + tableFrom + ", tableTo=" + tableTo + ")");
            }
            //判断该srcMysql和destMysql是否重复
            if (this.syncTaskActionService.existsBySrcAndDest(srcMysql, destMysql)) {
                throw new IllegalArgumentException("创建失败，已有相同的配置存在。(srcMysqlName=" + srcMysql + ", destMysqlName=" + destMysql
                        + ")");
            }
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
            session.setAttribute("mysqlMapping", mysqlMapping);
            MysqlConfig srcMysqlConfig = mysqlConfigService.find(srcMysql);
            MysqlConfig destMysqlConfig = mysqlConfigService.find(destMysql);
            session.setAttribute("srcMysqlConfig", srcMysqlConfig);
            session.setAttribute("destMysqlConfig", destMysqlConfig);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    @RequestMapping(method = RequestMethod.GET, value = { "/create/step2" })
    public ModelAndView step2(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //从session拿出srcMysql，destMysql查询mysql配置
        MysqlConfig srcMysqlConfig = (MysqlConfig) session.getAttribute("srcMysqlConfig");
        MysqlConfig destMysqlConfig = (MysqlConfig) session.getAttribute("destMysqlConfig");
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();
        //从会话中取出保存的mysqlMapping，计算出dumpMapping
        MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
        DumpMapping dumpMapping = this.syncTaskActionService.convertMysqlMappingToDumpMapping(srcMysqlConfig.getHosts().get(0),
                mysqlMapping);
        session.setAttribute("dumpMapping", dumpMapping);

        map.put("srcMysqlConfig", srcMysqlConfig);
        map.put("destMysqlConfig", destMysqlConfig);
        map.put("syncServerConfigs", syncServerConfigs);
        map.put("dumpMapping", dumpMapping);
        map.put("createActive", "active");
        map.put("path", "create");
        map.put("subPath", "step2");
        return new ModelAndView("main/container", map);
    }

    /**
     * 创建DumpAction
     */
    @RequestMapping(value = "/create/createDumpAction", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createDumpAction(HttpSession session, String srcMysqlHost, String destMysqlHost, String syncServerName) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //检查参数
            if (StringUtils.isBlank(srcMysqlHost)) {
                throw new IllegalArgumentException("srcMysqlHost不能为空");
            }
            if (StringUtils.isBlank(destMysqlHost)) {
                throw new IllegalArgumentException("destMysqlHost不能为空");
            }
            if (StringUtils.isBlank(syncServerName)) {
                throw new IllegalArgumentException("syncServerHost不能为空");
            }
            //从session拿出srcMysql，destMysql查询mysql配置
            MysqlConfig srcMysqlConfig = (MysqlConfig) session.getAttribute("srcMysqlConfig");
            MysqlConfig destMysqlConfig = (MysqlConfig) session.getAttribute("destMysqlConfig");
            DumpMapping dumpMapping = (DumpMapping) session.getAttribute("dumpMapping");
            //根据选择的srcMysqlHost，找出其MysqlHost对象
            MysqlHost srcMysqlHost0 = getMysqlHost(srcMysqlConfig, srcMysqlHost);
            //根据选择的destMysqlHost，找出其MysqlHost对象
            MysqlHost destMysqlHost0 = getMysqlHost(destMysqlConfig, destMysqlHost);
            //查询所有syncServer
            DumpTask dumpAction = new DumpTask();
            dumpAction.setSrcMysqlName(srcMysqlConfig.getName());
            dumpAction.setSrcMysqlHost(srcMysqlHost0);
            dumpAction.setDestMysqlName(destMysqlConfig.getName());
            dumpAction.setDestMysqlHost(destMysqlHost0);
            dumpAction.setDumpMapping(dumpMapping);
            dumpAction.setSyncServerName(syncServerName);
            //保存dumpAction到数据库
            dumpActionService.create(dumpAction);
            //保存dumpAction到session
            session.setAttribute("dumpActionId", dumpAction.getId());
            session.setAttribute("srcMysqlHost", srcMysqlHost0);//创建SyncTaskAction需要从srcMysqlHost0获取serverId显示到页面
            LOG.info("created dumpAction: " + dumpAction);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    /**
     * 查看DumpAction的状态
     */
    @RequestMapping(value = "/create/refleshDumpState", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object refleshDumpState(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Long id = (Long) session.getAttribute("dumpActionId");
            //检查参数
            if (id == null) {
                throw new IllegalArgumentException("dumpActionId为空，可能是会话已经过期！");
            }
            //查询dumpActionId对应的DumpActionState
            DumpTask dumpAction = this.dumpActionService.find(id);
            TaskState actionState = dumpAction.getTaskState();
            if (actionState != null) {
                map.put("dumpActionState", actionState);
                if (actionState.getBinlogInfo() != null) {
                    session.setAttribute("binlogInfo", actionState.getBinlogInfo());
                }
            } else {
                throw new IllegalArgumentException("dumpActionState不存在，请管理员查看什么原因！");
            }

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    @RequestMapping(method = RequestMethod.GET, value = { "/create/step3" })
    public ModelAndView step3(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //从session拿出
        MysqlConfig srcMysqlConfig = (MysqlConfig) session.getAttribute("srcMysqlConfig");
        MysqlConfig destMysqlConfig = (MysqlConfig) session.getAttribute("destMysqlConfig");
        MysqlHost srcMysqlHost = (MysqlHost) session.getAttribute("srcMysqlHost");
        BinlogInfo binlogInfo = (BinlogInfo) session.getAttribute("binlogInfo");
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();

        map.put("srcMysqlConfig", srcMysqlConfig);
        map.put("destMysqlConfig", destMysqlConfig);
        map.put("srcMysqlHost", srcMysqlHost);
        map.put("syncServerConfigs", syncServerConfigs);
        map.put("binlogInfo", binlogInfo);
        map.put("createActive", "active");
        map.put("path", "create");
        map.put("subPath", "step3");
        return new ModelAndView("main/container", map);
    }

    /**
     * 创建SyncAction
     * 
     * @param ddl
     * @param pumaClientName
     * @param serverId
     * @param transaction
     */
    @RequestMapping(value = "/create/createSyncAction", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createSyncAction(HttpSession session, String syncServerName, String destMysqlHost, String binlogFile,
                                   String binlogPosition, Boolean ddl, Boolean dml, String pumaClientName, Long serverId,
                                   Boolean transaction) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //检查参数
            if (StringUtils.isBlank(syncServerName)) {
                throw new IllegalArgumentException("syncServerHost不能为空");
            }
            if (StringUtils.isBlank(destMysqlHost)) {
                throw new IllegalArgumentException("destMysqlHost不能为空");
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
            if (serverId == null) {
                throw new IllegalArgumentException("serverId不能为空");
            }
            //从session拿出
            MysqlConfig srcMysqlConfig = (MysqlConfig) session.getAttribute("srcMysqlConfig");
            MysqlConfig destMysqlConfig = (MysqlConfig) session.getAttribute("destMysqlConfig");
            MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
            //创建SyncAction
            SyncTask syncTaskAction = new SyncTask();
            syncTaskAction.setSrcMysqlName(srcMysqlConfig.getName());
            syncTaskAction.setDestMysqlName(destMysqlConfig.getName());
            syncTaskAction.setDestMysqlHost(getMysqlHost(destMysqlConfig, destMysqlHost));
            syncTaskAction.setMysqlMapping(mysqlMapping);
            syncTaskAction.setSyncServerName(syncServerName);
            BinlogInfo binlogInfo = new BinlogInfo();
            if (StringUtils.isNotBlank(binlogFile) && StringUtils.isNotBlank(binlogPosition)) {
                binlogInfo.setBinlogFile(binlogFile);
                binlogInfo.setBinlogPosition(Long.parseLong(binlogPosition));
            }
            syncTaskAction.setBinlogInfo(binlogInfo);
            syncTaskAction.setDdl(ddl != null ? ddl : true);
            syncTaskAction.setDml(dml != null ? dml : true);
            syncTaskAction.setPumaClientName(pumaClientName);
            syncTaskAction.setServerId(serverId);
            syncTaskAction.setTransaction(transaction != null ? transaction : true);
            Date curDate = new Date();
            syncTaskAction.setCreateTime(curDate);
            syncTaskAction.setLastUpdateTime(curDate);
            //保存dumpAction到数据库
            syncTaskActionService.create(syncTaskAction);
            //更新dumpAction的syncTaskId
            Long syncTaskId = syncTaskAction.getId();
            Long dumpActionId = (Long) session.getAttribute("dumpActionId");
            this.dumpActionService.updateSyncTaskId(dumpActionId, syncTaskId);
            LOG.info("syncTaskAction.getId(): " + syncTaskAction.getId());

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
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

}
