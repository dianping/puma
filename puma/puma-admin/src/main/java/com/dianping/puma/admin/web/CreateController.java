package com.dianping.puma.admin.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXParseException;

import com.dianping.puma.admin.config.PropertiesConfig;
import com.dianping.puma.admin.service.DumpActionService;
import com.dianping.puma.admin.service.DumpActionStateService;
import com.dianping.puma.admin.service.MysqlConfigService;
import com.dianping.puma.admin.service.PumaSyncServerConfigService;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.admin.util.HttpClientUtil;
import com.dianping.puma.admin.util.SyncXmlParser;
import com.dianping.puma.core.sync.DumpConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.SyncTask;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.action.DumpAction;
import com.dianping.puma.core.sync.model.action.DumpActionState;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.google.gson.Gson;

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
    @Autowired
    private SyncConfigService syncConfigService;
    @Autowired
    private MysqlConfigService mysqlConfigService;
    @Autowired
    private DumpActionService dumpActionService;
    @Autowired
    private DumpActionStateService dumpActionStateService;
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

    @RequestMapping(value = "/create/loadXmlSample", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadXmlSample() {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String sampleXml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SyncSample.xml"), "UTF-8");
            map.put("sampleXml", sampleXml);
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

    @RequestMapping(value = "/create/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save(HttpSession session, String srcMysql, String destMysql, String syncXml) {
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
        DumpMapping dumpMapping = this.syncConfigService.convertMysqlMappingToDumpMapping(srcMysqlConfig.getHosts().get(0),
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
            //查询所有syncServer
            DumpAction dumpAction = new DumpAction();
            dumpAction.setSrcMysqlName(srcMysqlConfig.getName());
            dumpAction.setSrcMysqlHost(srcMysqlHost);
            dumpAction.setDestMysqlName(destMysqlConfig.getName());
            dumpAction.setDestMysqlHost(destMysqlHost);
            dumpAction.setDumpMapping(dumpMapping);
            dumpAction.setSyncServerName(syncServerName);
            //保存dumpAction到数据库
            dumpActionService.create(dumpAction);
            //保存dumpAction到session
            session.setAttribute("dumpActionId", dumpAction.getId());
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
            ObjectId id = (ObjectId) session.getAttribute("dumpActionId");
            //检查参数
            if (id == null) {
                throw new IllegalArgumentException("dumpActionId为空，可能是会话已经过期！");
            }
            //查询dumpActionId对应的DumpActionState
            DumpActionState dumpActionState = this.dumpActionStateService.find(id);
            if (dumpActionState != null) {
                map.put("dumpActionState", dumpActionState);
                if (dumpActionState.getBinlogInfo() != null) {
                    session.setAttribute("binlogInfo", dumpActionState.getBinlogInfo());
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
        BinlogInfo binlogInfo = (BinlogInfo) session.getAttribute("binlogInfo");
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();

        map.put("srcMysqlConfig", srcMysqlConfig);
        map.put("destMysqlConfig", destMysqlConfig);
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
            SyncTaskAction syncTaskAction = new SyncTaskAction();
            syncTaskAction.setSrcMysqlName(srcMysqlConfig.getName());
            syncTaskAction.setDestMysqlName(destMysqlConfig.getName());
            syncTaskAction.setDestMysqlHost(destMysqlHost);
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
            //保存dumpAction到session
            session.setAttribute("dumpActionId", syncTaskAction.getId());
            LOG.info("created dumpAction: " + syncTaskAction);

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

    //*****************************************************************************
    /**
     * 如果mergeId为空，那么新增配置；不为空，则修改配置。
     */
    @RequestMapping(value = "/saveSyncXml", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object saveSyncXml(HttpSession session, String srcMysql, String destMysql, String syncXml) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //解析xml，得到SyncConfig
            SyncConfig syncConfig = SyncXmlParser.parse(syncXml);
            syncConfig.setId(new ObjectId());
            LOG.info("create SyncConfig: " + syncConfig);
            //保存SyncConfig到db,同时保存SyncXml
            map.put("id", syncConfigService.saveSyncConfig(syncConfig, syncXml));
            //保存syncConfig到session
            session.setAttribute("syncConfig", syncConfig);

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

    @RequestMapping(value = "/loadDumpConfig", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadDumpConfig(HttpSession session, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //从会话中取出保存的syncConfig
            SyncConfig syncConfig = (SyncConfig) session.getAttribute("syncConfig");
            System.out.println(syncConfig);
            //将syncConfig转化成dumpConfig
            DumpConfig dumpConfig = this.syncConfigService.convertSyncConfigToDumpConfig(syncConfig);
            //将objectId和dumpConfig放到session中
            session.setAttribute("dumpConfig", dumpConfig);

            map.put("dumpConfig", dumpConfig);
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

    @RequestMapping(value = "/dump", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object dump(HttpSession session, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //從session中获取dumpConfig
            DumpConfig dumpConfig = (DumpConfig) session.getAttribute("dumpConfig");
            String dumpServiceUrl = "http://" + PropertiesConfig.getInstance().getDumpServerHost() + "/puma-syncserver/dump";
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("sessionId", session.getId()));
            nvps.add(new BasicNameValuePair("dumpConfigJson", GsonUtil.toJson(dumpConfig)));
            //将dumpConfig序列化为json，发送给sync-server
            InputStream ins = HttpClientUtil.postForStream(dumpServiceUrl, nvps);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
            //            LOG.info(IOUtils.toString(ins));
            //将返回的流存储起来
            BufferedReader oldReader = (BufferedReader) session.getAttribute("dumpReader");
            if (oldReader != null) {
                oldReader.close();
            }
            session.setAttribute("dumpReader", reader);
            map.put("dumpConfig", dumpConfig);
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
     * js使用长polling不断调用console()。<br>
     */
    @RequestMapping(value = "/dumpConsole", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object dumpConsole(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BufferedReader reader = (BufferedReader) session.getAttribute("dumpReader");
            String status = "continue";
            if (reader == null) {
                status = "done";
            } else {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        status = "done";
                        reader.close();
                        session.removeAttribute("dumpReader");
                    } else {
                        map.put("content", line + "\n");
                    }
                } catch (IOException e) {//在任何异常时停止进程(如果已经不在运行，也会抛异常)
                    status = "done";
                    reader.close();
                    session.removeAttribute("dumpReader");
                }
            }
            map.put("status", status);
            map.put("success", true);
        } catch (Exception e) {
            StringBuilder error = new StringBuilder();
            error.append(e.getMessage()).append("\n");
            for (StackTraceElement element : e.getStackTrace()) {
                error.append(element.toString()).append("\n");
            }
            LOG.error(e.getMessage(), e);
            map.put("success", false);
            map.put("errorMsg", error);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * 将binlog位置信息更新到相应的SyncConfig
     */
    @RequestMapping(value = "/saveBinlog", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object saveBinlog(HttpSession session, HttpServletRequest request, String binlogFile, Long binlogPosition) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(binlogFile)) {
                throw new IllegalArgumentException("binlogFile不能为空！");
            }
            if (binlogPosition == null || binlogPosition < 0) {
                throw new IllegalArgumentException("binlogFile不能为空或小于0！");
            }
            //从会话中取出保存的syncConfig
            SyncConfig syncConfig = (SyncConfig) session.getAttribute("syncConfig");
            //更新binlogInfo
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(binlogFile);
            binlogInfo.setBinlogPosition(binlogPosition);
            this.syncConfigService.modifySyncConfig(syncConfig.getId(), binlogInfo);
            //更新成功后，更新session的syncConfig
            syncConfig.getSrc().setBinlogInfo(binlogInfo);

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

    @RequestMapping(value = "/loadSyncServerList", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadSyncServerList(HttpSession session, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            // 加载SyncServer的服务器列表
            List<String> syncServerHosts = PropertiesConfig.getInstance().getSyncServerHosts();
            map.put("syncServerHosts", syncServerHosts);
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
     * 创建同步任务,需指派PumaSyncServer的id(保存到数据库，暂时不启动)
     */
    @RequestMapping(value = "/saveSyncTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object saveSyncTask(HttpSession session, HttpServletRequest request, String syncServerHost) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //验证
            if (StringUtils.isBlank(syncServerHost)) {
                throw new IllegalArgumentException("syncServerHost不能为空！");
            }
            // 根据syncConfig和pumaSyncServerId，创建SyncTask
            SyncTask task = new SyncTask();
            SyncConfig syncConfig = (SyncConfig) session.getAttribute("syncConfig");
            task.setCreateDate(new Date());
            task.setId(new ObjectId());
            task.setSyncConfigId(syncConfig.getId());
            task.setSyncServerHost(syncServerHost);
            //保存SyncTask
            syncConfigService.saveSyncTask(task);

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
     * 启动同步任务(向指派的PumaSyncServer发出启动同步任务的命令)
     */
    @RequestMapping(value = "/startTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object startTask(HttpSession session, String taskId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //TODO 调用puma-syncserver 参数是taskId

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

}
