package com.dianping.puma.admin.web;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.service.CatchupTaskService;
import com.dianping.puma.admin.service.DumpTaskService;
import com.dianping.puma.admin.service.MysqlConfigService;
import com.dianping.puma.admin.service.PumaSyncServerConfigService;
import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;

/**
 * TODO <br>
 * (1) 以create为整个controller，所有中间状态存放在session<br>
 * (2) 编写SyncTask的service <br>
 * (3) 保存binlog信息<br>
 * (4) pumaSyncServer的host的选择 (5) 创建同步任务，启动任务
 * 
 * @author wukezhu
 */
@Controller
public class ModifyController {
    private static final Logger LOG = LoggerFactory.getLogger(ModifyController.class);
    @Autowired
    private MysqlConfigService mysqlConfigService;
    @Autowired
    private DumpTaskService dumpTaskService;
    @Autowired
    private PumaSyncServerConfigService pumaSyncServerConfigService;
    @Autowired
    private SyncTaskService syncTaskService;
    @Autowired
    private CatchupTaskService catchupTaskService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";
    private static final int PAGESIZE = 8;

    @RequestMapping(value = { "/modify" })
    public ModelAndView modify(HttpServletRequest request, HttpServletResponse response) {
        return modify0(request, response, 1);
    }

    @RequestMapping(value = { "/modify/p/{pageNum}" })
    public ModelAndView modify0(HttpServletRequest request, HttpServletResponse response, @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        //        System.out.println(syncConfigService.find());
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<SyncTask> syncTaskActions = syncTaskService.find(offset, PAGESIZE);
        map.put("syncTaskActions", syncTaskActions);
        map.put("modifyActive", "active");
        map.put("subPath", "view");
        map.put("path", "modify");
        return new ModelAndView("main/container", map);
    }

    /**
     * 修改SyncTaskActionState的页面
     */
    @RequestMapping(value = "/modify/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView action(HttpSession session, @PathVariable("id") Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTask syncTaskAction = this.syncTaskService.find(actionId);
        TaskState actionState = syncTaskAction.getTaskState();
        session.setAttribute("syncTaskAction", syncTaskAction);
        session.setAttribute("syncTaskActionState", actionState);
        map.put("modifyActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step1");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "/modify/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save(HttpSession session, String[] databaseFrom, String[] databaseTo, String[] tableFrom, String[] tableTo,
                            Integer count[]) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //验证参数
            //databaseFrom和databaseTo
            if (!(databaseFrom != null && databaseTo != null && databaseFrom.length == databaseTo.length)) {
                throw new IllegalArgumentException("源数据库个数和目标数据库的个数不一致。(databaseFrom个数=" + databaseFrom.length + ", databaseTo个数="
                        + databaseTo.length + ")");
            }
            //count
            if (count == null || count.length != databaseFrom.length) {
                throw new IllegalArgumentException("count参数个数和源数据库个数不一致。(database个数=" + databaseFrom.length + ", count个数="
                        + count.length + ")");
            }
            int totalTable = 0;
            for (int c : count) {
                totalTable += c;
            }
            //tableFrom和tableTo,totalTable
            if (tableFrom == null && tableTo == null) {
                tableFrom = new String[] { "*" };
                tableTo = new String[] { "*" };
            } else if (!(tableFrom != null && tableTo != null && tableFrom.length == tableTo.length)) {
                throw new IllegalArgumentException("源表个数和目标表的个数不一致。(tableFrom个数=" + tableFrom.length + ", tableTo个数="
                        + tableTo.length + ")");
            } else if (tableFrom.length != totalTable) {
                throw new IllegalArgumentException("count参数总和与表的个数不一致。(table个数=" + tableFrom.length + ", count总和=" + totalTable
                        + ")");
            }
            //解析mapping
            MysqlMapping mysqlMapping = new MysqlMapping();
            int j = 0, countAmount = 0;
            for (int i = 0; i < databaseFrom.length; i++) {
                String dbFrom = databaseFrom[i];
                String dbTo = databaseTo[i];
                DatabaseMapping database = new DatabaseMapping();
                database.setFrom(dbFrom);
                database.setTo(dbTo);
                countAmount += count[i];
                for (; j < countAmount; j++) {
                    String tableFrom0 = tableFrom[j];
                    String tableTo0 = tableTo[j];
                    TableMapping table = new TableMapping();
                    table.setFrom(tableFrom0);
                    table.setTo(tableTo0);
                    database.addTable(table);
                }
                mysqlMapping.addDatabase(database);
            }
            //对比新的mapping和现有的mapping
            SyncTask syncTaskAction = (SyncTask) session.getAttribute("syncTaskAction");
            MysqlMapping oldMysqlMapping = syncTaskAction.getMysqlMapping();
            MysqlMapping additionalMysqlMapping = this.syncTaskService.compare(oldMysqlMapping, mysqlMapping);

            //保存到session
            session.setAttribute("mysqlMapping", mysqlMapping);
            session.setAttribute("additionalMysqlMapping", additionalMysqlMapping);

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

    @RequestMapping(method = RequestMethod.GET, value = { "/modify/step2" })
    public ModelAndView step2(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //从session拿出srcMysql，destMysql查询mysql配置
        SyncTask syncTaskAction = (SyncTask) session.getAttribute("syncTaskAction");
        String srcMysqlName = syncTaskAction.getSrcMysqlName();
        MysqlConfig srcMysqlConfig = this.mysqlConfigService.find(srcMysqlName);
        String destMysqlName = syncTaskAction.getDestMysqlName();
        MysqlConfig destMysqlConfig = this.mysqlConfigService.find(destMysqlName);
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();
        //从会话中取出保存的mysqlMapping，计算出dumpMapping
        MysqlMapping additionalMysqlMapping = (MysqlMapping) session.getAttribute("additionalMysqlMapping");
        DumpMapping dumpMapping = this.syncTaskService.convertMysqlMappingToDumpMapping(srcMysqlConfig.getHosts().get(0),
                additionalMysqlMapping);
        session.setAttribute("dumpMapping", dumpMapping);

        //保存到session
        session.setAttribute("srcMysqlConfig", srcMysqlConfig);
        session.setAttribute("destMysqlConfig", destMysqlConfig);

        map.put("syncServerConfigs", syncServerConfigs);
        map.put("modifyActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step2");
        return new ModelAndView("main/container", map);
    }

    /**
     * 创建DumpAction
     */
    @RequestMapping(value = "/modify/createDumpAction", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createDumpAction(HttpSession session, String srcMysqlHost, String syncServerName) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //检查参数
            if (StringUtils.isBlank(srcMysqlHost)) {
                throw new IllegalArgumentException("srcMysqlHost不能为空");
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
            //获取destMysqlHost对象
            SyncTask syncTaskAction = (SyncTask) session.getAttribute("syncTaskAction");
            MysqlHost destMysqlHost0 = syncTaskAction.getDestMysqlHost();

            //查询所有syncServer
            DumpTask dumpAction = new DumpTask();
            dumpAction.setSrcMysqlName(srcMysqlConfig.getName());
            dumpAction.setSrcMysqlHost(srcMysqlHost0);
            dumpAction.setDestMysqlName(destMysqlConfig.getName());
            dumpAction.setDestMysqlHost(destMysqlHost0);
            dumpAction.setDumpMapping(dumpMapping);
            dumpAction.setSyncServerName(syncServerName);
            //保存dumpAction到数据库
            dumpTaskService.create(dumpAction);
            //保存dumpAction到session
            session.setAttribute("dumpActionId", dumpAction.getId());
            session.setAttribute("srcMysqlHost", srcMysqlHost0);//创建CatchupAction和SyncTaskAction需要从srcMysqlHost0获取serverId显示到页面
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
    @RequestMapping(value = "/modify/refleshDumpState", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
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
            DumpTask dumpAction = this.dumpTaskService.find(id);
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

    /**
     * 创建CatchupAction的页面
     */
    @RequestMapping(method = RequestMethod.GET, value = { "/modify/step3" })
    public ModelAndView step3(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();

        map.put("syncServerConfigs", syncServerConfigs);
        map.put("pumaClientName", "Catchup-" + UUID.randomUUID());
        map.put("modifyActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step3");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "/create/createCatchupTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createCatchupTask(HttpSession session, String binlogFile, String binlogPosition, String pumaClientName,
                                    Long serverId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //检查参数
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
            //从session拿出SyncTask
            SyncTask syncTask = (SyncTask) session.getAttribute("syncTaskAction");
            MysqlMapping mysqlMapping = (MysqlMapping) session.getAttribute("mysqlMapping");
            //创建
            CatchupTask catchupTask = new CatchupTask();
            catchupTask.setSrcMysqlName(syncTask.getSrcMysqlName());
            catchupTask.setDestMysqlName(syncTask.getDestMysqlName());
            catchupTask.setDestMysqlHost(syncTask.getDestMysqlHost());
            catchupTask.setMysqlMapping(mysqlMapping);
            catchupTask.setSyncServerName(syncTask.getSyncServerName());
            BinlogInfo binlogInfo = new BinlogInfo();
            if (StringUtils.isNotBlank(binlogFile) && StringUtils.isNotBlank(binlogPosition)) {
                binlogInfo.setBinlogFile(binlogFile);
                binlogInfo.setBinlogPosition(Long.parseLong(binlogPosition));
            }
            catchupTask.setBinlogInfo(binlogInfo);
            catchupTask.setDdl(syncTask.isDdl());
            catchupTask.setDml(syncTask.isDml());
            catchupTask.setPumaClientName(pumaClientName);
            catchupTask.setServerId(serverId);
            catchupTask.setTransaction(syncTask.isTransaction());
            catchupTask.setSyncTaskId(syncTask.getId());
            //保存到数据库
            catchupTaskService.create(catchupTask);
            //保存catchupTask到session
            session.setAttribute("catchupTaskId", catchupTask.getId());

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
     * 查看CatchupTask的状态
     */
    @RequestMapping(value = "/modify/refleshCatchupTaskState", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object refleshCatchupTaskState(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Long id = (Long) session.getAttribute("catchupTaskId");
            //检查参数
            if (id == null) {
                throw new IllegalArgumentException("catchupTaskId为空，可能是会话已经过期！");
            }
            //查询dumpActionId对应的DumpActionState
            CatchupTask catchupTask = this.catchupTaskService.find(id);
            TaskState taskState = catchupTask.getTaskState();
            map.put("catchupTaskState", taskState);
            if (taskState.getBinlogInfo() != null) {
                session.setAttribute("binlogInfo", taskState.getBinlogInfo());
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
