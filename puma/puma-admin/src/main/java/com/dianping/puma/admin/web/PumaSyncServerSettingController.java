package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.service.PumaSyncServerConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;

@Controller
public class PumaSyncServerSettingController {
    private static final Logger LOG = LoggerFactory.getLogger(PumaSyncServerSettingController.class);
    @Autowired
    private PumaSyncServerConfigService pumaSyncServerConfigService;

    @RequestMapping(value = { "/pumaSyncServerSetting" })
    public ModelAndView view() {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询PumaSyncServerConfig
        List<PumaSyncServerConfig> pumaSyncServerConfigs = pumaSyncServerConfigService.findAll();

        map.put("pumaSyncServerConfigs", pumaSyncServerConfigs);
        map.put("pumaSyncServerSettingActive", "active");
        map.put("path", "pumaSyncServerSetting");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/pumaSyncServerSetting/create" }, method = RequestMethod.GET)
    public ModelAndView create() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pumaSyncServerSettingActive", "active");
        map.put("path", "pumaSyncServerSetting");
        map.put("subPath", "create");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/pumaSyncServerSetting/modify" }, method = RequestMethod.GET)
    public ModelAndView modify(String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询PumaSyncServerConfig
        PumaSyncServerConfig pumaSyncServerConfig = this.pumaSyncServerConfigService.find(new ObjectId(id));
        map.put("pumaSyncServerConfig", pumaSyncServerConfig);
        map.put("pumaSyncServerSettingActive", "active");
        map.put("path", "pumaSyncServerSetting");
        map.put("subPath", "modify");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/pumaSyncServerSetting/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String createPost(String name, String host) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("name不能为空");
            }
            if (StringUtils.isBlank(host)) {
                throw new IllegalArgumentException("host不能为空");
            }
            //保存
            PumaSyncServerConfig pumaSyncServerConfig = new PumaSyncServerConfig();
            pumaSyncServerConfig.setName(name);
            pumaSyncServerConfig.setHost(host);
            this.pumaSyncServerConfigService.save(pumaSyncServerConfig);

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

    @RequestMapping(value = { "/pumaSyncServerSetting/modify" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String modifyPost(String id, String name, String host) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(id)) {
                throw new IllegalArgumentException("id不能为空");
            }
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("name不能为空");
            }
            if (StringUtils.isBlank(host)) {
                throw new IllegalArgumentException("host不能为空");
            }
            //保存
            PumaSyncServerConfig pumaSyncServerConfig = new PumaSyncServerConfig();
            pumaSyncServerConfig.setId(new ObjectId(id));
            pumaSyncServerConfig.setName(name);
            pumaSyncServerConfig.setHost(host);
            this.pumaSyncServerConfigService.save(pumaSyncServerConfig);

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

    @RequestMapping(value = { "/pumaSyncServerSetting/delete" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String delete(String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(id)) {
                throw new IllegalArgumentException("id不能为空");
            }
            //删除
            this.pumaSyncServerConfigService.remove(new ObjectId(id));

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
}
