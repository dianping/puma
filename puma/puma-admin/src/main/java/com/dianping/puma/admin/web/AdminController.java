package com.dianping.puma.admin.web;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.admin.util.MongoUtils;
import com.dianping.puma.admin.util.SyncXmlParser;
import com.dianping.puma.core.sync.SyncConfig;
import com.google.gson.Gson;

@Controller
public class AdminController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private SyncConfigService syncConfigService;

    /**
     * 首页，使用说明
     */
    @RequestMapping(value = { "/" })
    public ModelAndView readme(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("readmeActive", "active");
        map.put("path", "readme");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/create" })
    public ModelAndView create(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("createActive", "active");
        map.put("path", "create");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/modify" })
    public ModelAndView modify(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("modifyActive", "active");
        map.put("path", "modify");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/delete" })
    public ModelAndView delete(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("deleteActive", "active");
        map.put("path", "delete");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/watch" })
    public ModelAndView watch(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
//        System.out.println(syncConfigService.find());
        map.put("watchActive", "active");
        map.put("path", "watch");
        return new ModelAndView("main/container", map);
    }

    public String createSync(HttpServletRequest request, HttpServletResponse response, String syncJson) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //接收SyncConfig的json字符串，解析得到SyncConfig对象
            SyncConfig syncConfig = null;
            if (syncJson == null) {
                File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-admin/src/main/resources/sync.xml");
                String syncXml = IOUtils.toString(new FileInputStream(file), "UTF-8");
                //解析syncXml，得到Sync对象
                syncConfig = SyncXmlParser.parse(syncXml);
                syncConfig.setId(new ObjectId());
            } else {
                syncConfig = GsonUtil.fromJson(syncJson, SyncConfig.class);
            }
            LOG.info("receive sync: " + syncConfig);
//            syncConfigService.save(syncConfig); 

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
