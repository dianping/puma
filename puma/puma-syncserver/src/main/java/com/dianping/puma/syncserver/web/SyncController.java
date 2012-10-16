package com.dianping.puma.syncserver.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.syncserver.bo.SyncClient;
import com.dianping.puma.syncserver.util.SyncXmlParser;
import com.google.gson.Gson;

@Controller
public class SyncController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);

    @RequestMapping(value = "/createSync", method = { RequestMethod.GET, RequestMethod.POST }, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createSync(String syncXml) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //TODO mock syncxml
            File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-syncserver/src/main/resources/sync.xml");
            syncXml = IOUtils.toString(new FileInputStream(file), "UTF-8");
            //解析syncXml，得到Sync对象
            SyncConfig sync = SyncXmlParser.parse(syncXml);
            System.out.println(sync);
            //启动SyncClient对象
            SyncClient syncClient = new SyncClient();
            syncClient.setSync(sync);
            syncClient.start();

            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", stackToString(e));
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
    @RequestMapping(value = "/modifySync", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object modifySync(HttpServletRequest request, String syncXml) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //解析syncXml，得到Sync对象
            SyncConfig sync = SyncXmlParser.parse(syncXml);

            //启动SyncClient对象

            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", "对不起，服务器内部错误。");
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);

    }

    private String stackToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
