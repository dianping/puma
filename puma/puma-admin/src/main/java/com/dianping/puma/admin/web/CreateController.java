package com.dianping.puma.admin.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.xml.sax.SAXParseException;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.admin.config.PropertiesConfig;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.admin.util.HttpClientUtil;
import com.dianping.puma.admin.util.SyncXmlParser;
import com.dianping.puma.core.sync.Constant;
import com.dianping.puma.core.sync.DatabaseBinlogInfo;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.DumpConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;

/**
 * 
 * TODO <br>
 * (1) 以create为整个controller，所有中间状态存放在session
 * (2) 编写SyncTask的service
 * (3) pumaSyncServer的id与host的映射
 * (4) 保存binlog信息，创建同步任务，启动任务
 * 
 * @author wukezhu
 */
@Controller
@RequestMapping(method = RequestMethod.POST, produces = "application/json; charset=utf-8")
public class CreateController {
    private static final Logger LOG = LoggerFactory.getLogger(CreateController.class);
    @Autowired
    private SyncConfigService syncConfigService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";
    private static final int PAGESIZE = 8;

    @RequestMapping(value = "/loadSyncConfigs", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadSyncConfigs(HttpSession session, HttpServletRequest request, Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
            List<SyncConfig> syncConfigs = syncConfigService.findSyncConfigs(offset, PAGESIZE);
            Long totalSyncConfig = syncConfigService.countSyncConfigs();
            map.put("syncConfigs", syncConfigs);
            map.put("totalPage", totalSyncConfig / PAGESIZE + (totalSyncConfig % PAGESIZE == 0 ? 0 : 1));
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

    @RequestMapping(value = "/loadSyncXml", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadSyncXml(HttpSession session, HttpServletRequest request, String mergeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String[] mergeIdSplits = StringUtils.split(mergeId, '_');
            int inc = Integer.parseInt(mergeIdSplits[0]);
            int machine = Integer.parseInt(mergeIdSplits[1]);
            int time = Integer.parseInt(mergeIdSplits[2]);
            ObjectId objectId = new ObjectId(time, machine, inc);
            //mergeId解析成ObjectId
            SyncXml syncXml = syncConfigService.findSyncXml(objectId);

            map.put("syncXml", syncXml);
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

    @RequestMapping(value = "/loadDumpConfig", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadDumpConfig(HttpSession session, HttpServletRequest request, String mergeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String[] mergeIdSplits = StringUtils.split(mergeId, '_');
            int inc = Integer.parseInt(mergeIdSplits[0]);
            int machine = Integer.parseInt(mergeIdSplits[1]);
            int time = Integer.parseInt(mergeIdSplits[2]);
            ObjectId objectId = new ObjectId(time, machine, inc);
            //mergeId解析成ObjectId
            SyncConfig syncConfig = syncConfigService.findSyncConfig(objectId);
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

    @RequestMapping(value = "/saveSyncXml", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object saveSyncXml(HttpSession session, HttpServletRequest request, String syncXmlString) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //解析xml，得到SyncConfig
            SyncConfig syncConfig = null;
            //解析syncXml，得到Sync对象
            syncConfig = SyncXmlParser.parse(syncXmlString);
            syncConfig.setId(new ObjectId());
            LOG.info("receive sync: " + syncConfig);
            //保存SyncConfig到db,同时保存SyncXml
            map.put("id", syncConfigService.saveSyncConfig(syncConfig, syncXmlString));

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

    @RequestMapping(value = "/modifySyncXml", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object modifySyncXml(HttpSession session, HttpServletRequest request, String syncXmlString, String mergeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //获取ObjectId
            String[] mergeIdSplits = StringUtils.split(mergeId, '_');
            int inc = Integer.parseInt(mergeIdSplits[0]);
            int machine = Integer.parseInt(mergeIdSplits[1]);
            int time = Integer.parseInt(mergeIdSplits[2]);
            ObjectId objectId = new ObjectId(time, machine, inc);
            //解析xml，得到SyncConfig
            SyncConfig syncConfig = SyncXmlParser.parse(syncXmlString);
            LOG.info("receive sync: " + syncConfig);
            //保存修改
            syncConfigService.modifySyncConfig(objectId, syncConfig, syncXmlString);

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

    @RequestMapping(value = "/dump", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object dump(HttpSession session, HttpServletRequest request, String mergeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //從session中获取dumpConfig
            DumpConfig dumpConfig = (DumpConfig) session.getAttribute("dumpConfig");
            String url = PropertiesConfig.getInstance().getPumaSyncServerIp();
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("sessionId", session.getId()));
            nvps.add(new BasicNameValuePair("dumpConfigJson", GsonUtil.toJson(dumpConfig)));
            //将dumpConfig序列化为json，发送给sync-server
            InputStream ins = HttpClientUtil.postForStream(url, nvps);
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
    public Object saveBinlog(HttpSession session, HttpServletRequest request, String syncConfigMergeId, String binlogFile,
                             long binlogPos) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
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
    @RequestMapping(value = "/createTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createTask(HttpSession session, HttpServletRequest request, String syncConfigMergeId, String pumaSyncServerId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
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
