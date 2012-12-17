package com.dianping.puma.syncserver.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.puma.core.sync.BinlogInfo;
import com.dianping.puma.core.sync.Constant;
import com.dianping.puma.core.sync.DatabaseBinlogInfo;
import com.dianping.puma.core.sync.DumpConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.syncserver.bo.AbstractSyncClient;
import com.dianping.puma.syncserver.bo.CatchupClient;
import com.dianping.puma.syncserver.bo.DumpClient;
import com.dianping.puma.syncserver.bo.SyncClient;
import com.dianping.puma.syncserver.holder.SyncClientHolder;
import com.dianping.puma.syncserver.service.SyncConfigService;
import com.dianping.puma.syncserver.util.GsonUtil;
import com.dianping.puma.syncserver.util.SyncXmlParser;
import com.google.gson.Gson;

/**
 * 先做暂停和追赶<br>
 * 1.core的sync模块移到新的Puma-SyncCore模块，增加DAO层，读写sync和dump(存放到mongo) <br>
 * 2.admin通过DAO层读写sync和dump配置 <br>
 * 3.SyncServer通过DAO层读sync和dump配置<br>
 * 4.SyncServer启动后连接admin，admin告之该获取哪些SyncConfig配置(通过dao获取)
 * 
 * @author wukezhu
 */
@Controller
public class SyncController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private SyncConfigService syncConfigService;

    @RequestMapping(value = "/startTask", method = { RequestMethod.GET, RequestMethod.POST }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object startTask(String syncTaskIdJson) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //接收syncTaskId的json字符串，解析得到syncTaskId
            ObjectId syncTaskId = GsonUtil.fromJson(syncTaskIdJson, ObjectId.class);
            SyncConfig syncConfig = this.syncConfigService.findSyncConfig(syncTaskId);
            boolean exist = SyncClientHolder.contain(syncConfig.getId());
            if (exist) {
                throw new IllegalArgumentException("SyncClient[SyncConfigId=" + syncConfig.getId() + "] is already running!");
            }
            //创建并启动SyncClient对象
            SyncClient syncClient = new SyncClient(syncConfig);
            syncClient.start();
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);

    }

    @RequestMapping(value = "/createSync", method = { RequestMethod.GET, RequestMethod.POST }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createSync(String syncJson) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //接收SyncConfig的json字符串，解析得到SyncConfig对象
            SyncConfig syncConfig = null;
            if (syncJson == null) {
                File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-syncserver/src/main/resources/sync.xml");
                String syncXml = IOUtils.toString(new FileInputStream(file), "UTF-8");
                //解析syncXml，得到Sync对象
                syncConfig = SyncXmlParser.parse(syncXml);
                syncConfig.setId(new ObjectId());
            } else {
                syncConfig = GsonUtil.fromJson(syncJson, SyncConfig.class);
            }
            LOG.info("receive sync: " + syncConfig);
            //判断SyncConfig对象是否已经存在，如果存在，返回错误
            boolean exist = SyncClientHolder.contain(syncConfig.getId());
            if (exist) {
                throw new IllegalArgumentException("SyncConfig[id=" + syncConfig.getId() + "] is already exist!");
            }
            //创建并启动SyncClient对象
            SyncClient syncClient = new SyncClient(syncConfig);
            syncClient.start();

            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);

    }

    /**
     * syncXml是更新后的配置。<br>
     * (1)验证：<br>
     * 新增database->database或table->table不能和原来的被包含关系，<br>
     * 被包含，如原来已有db1-> db1，就不能有db1.t1 -> db1.t1；<br>
     * 可以有覆盖，如原来已有db1.t1 -> db1.t1，可以有db1 -> db1，这样，需要访问db1的table然后除掉t1,再做mysqldump table的操作。 db1.t1；<br>
     * (2)mysqldump <br>
     * 找到新增的database->database和database.table -> database.table，进行mysqldump,返回dump的binlog位置<br>
     * (3)暂停SyncClient与启动临时SyncClient追赶<br>
     * 暂停当前的同步，记下binlog，启动临时的同步，只同步新增的database->database和database.table->database.table，追赶到binlog位置<br>
     * (4)启动新的SyncClient<br>
     * 使用新的syncXml设置SyncClient，恢复同步。<br>
     */
    @RequestMapping(value = "/modifySync", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object modifySync(HttpServletRequest request, String syncJson) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //接收SyncConfig的json字符串，解析得到SyncConfig对象
            SyncConfig syncConfig = null;
            if (syncJson == null) {
                File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-syncserver/src/main/resources/sync.xml");
                String syncXml = IOUtils.toString(new FileInputStream(file), "UTF-8");
                //解析syncXml，得到Sync对象
                syncConfig = SyncXmlParser.parse(syncXml);
                syncConfig.setId(new ObjectId());
            } else {
                syncConfig = GsonUtil.fromJson(syncJson, SyncConfig.class);
            }
            LOG.info("receive sync: " + syncConfig);
            //获取SyncClient对象
            SyncClient syncClient = (SyncClient) SyncClientHolder.get(syncConfig.getId());
            if (syncClient == null) {
                throw new IllegalArgumentException("SyncConfig[id=" + syncConfig.getId() + "] match No SyncClient, is not exist!");
            }
            //修改syncClient的SyncConfig
            syncClient.setSync(syncConfig);

            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * 根据dumpConfig，进行dump，并返回binlog位置<br>
     * (dump使用json，因为是内部传输；sync使用xml，因为是需要给用户看和修改。) <br>
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/dump", method = { RequestMethod.POST, RequestMethod.GET }, produces = "text/html; charset=utf-8")
    public void dump(HttpServletResponse response, String dumpConfigJson, String sessionId) throws UnsupportedEncodingException,
            IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"), true);
        try {
            //TODO mock dumpJson
            if (dumpConfigJson == null) {
                File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-syncserver/src/main/resources/dumpConfig.json");
                dumpConfigJson = IOUtils.toString(new FileInputStream(file), "UTF-8");
            }
            //解析dumpJson，得到DumpConfig对象
            Gson gson = new Gson();
            DumpConfig dumpConfig = gson.fromJson(dumpConfigJson, DumpConfig.class);
            LOG.info("sync-server receive dumpConfig: " + dumpConfig);
            pw.println("sync-server receive dumpConfig: " + dumpConfig);
            //启动DumpClient对象(DumpClient将进度输出到out)
            DumpClient dumpClient = new DumpClient(dumpConfig, pw, sessionId);
            pw.println("sync-server starting dump");
            List<DatabaseBinlogInfo> binlogInfos = dumpClient.dump();
            //将binlog输出
            //目前只支持一个databaseConfig，所以此处binlogInfos只有1个
            DatabaseBinlogInfo binlogInfo = binlogInfos.get(0);
            pw.println(Constant.BINLOG_SIGN_PREFIX + gson.toJson(binlogInfo));
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            pw.println(e.getMessage());
        } finally {
            pw.close();
        }
    }

    /**
     * 暂停SyncClient，并返回binlogInfo<br>
     */
    @RequestMapping(value = "/stop", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object stop(HttpServletRequest request, ObjectId syncConfigId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SyncClient syncClient = (SyncClient) SyncClientHolder.get(syncConfigId);
            BinlogInfo binlogInfo = syncClient.stop();
            map.put("binlogInfo", binlogInfo);
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * 创建新的SyncClient追赶，从binlogFrom 到 binlogTo <br>
     */
    @RequestMapping(value = "/catchup", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object catchup(HttpServletRequest request, String catchupJson, String startedBinlogFile, Long startedBinlogPosition,
                          String endBinlogFile, Long endBinlogPosition) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            SyncConfig syncConfig = null;
            if (catchupJson == null) {
                File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-syncserver/src/main/resources/sync.xml");
                String syncXml = IOUtils.toString(new FileInputStream(file), "UTF-8");
                //解析syncXml，得到Sync对象
                syncConfig = SyncXmlParser.parse(syncXml);
                syncConfig.setId(new ObjectId());
            } else {
                syncConfig = GsonUtil.fromJson(catchupJson, SyncConfig.class);
            }
            LOG.info("receive sync: " + syncConfig);
            //获取binlog
            BinlogInfo startedBinlogInfo = new BinlogInfo();
            startedBinlogInfo.setBinlogFile(startedBinlogFile);
            startedBinlogInfo.setBinlogPosition(startedBinlogPosition);
            BinlogInfo endBinlogInfo = new BinlogInfo();
            endBinlogInfo.setBinlogFile(endBinlogFile);
            endBinlogInfo.setBinlogPosition(endBinlogPosition);
            //构造CatchupClient对象
            CatchupClient catchupClient = new CatchupClient(syncConfig, startedBinlogInfo, endBinlogInfo);
            catchupClient.start();

            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * 查看状态<br>
     */
    @RequestMapping(value = "/status", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object status(HttpServletRequest request, ObjectId syncConfigId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            AbstractSyncClient syncClient = SyncClientHolder.get(syncConfigId);
            Object curBinlogInfo = syncClient.getCurBinlogInfo();
            map.put("binlogInfo", curBinlogInfo);
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

}
