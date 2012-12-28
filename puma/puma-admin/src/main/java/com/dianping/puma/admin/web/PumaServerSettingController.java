package com.dianping.puma.admin.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.service.MysqlConfigService;
import com.dianping.puma.admin.service.PumaServerConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.config.PumaServerConfig;

@Controller
public class PumaServerSettingController {
    private static final Logger LOG = LoggerFactory.getLogger(PumaServerSettingController.class);
    @Autowired
    private PumaServerConfigService pumaServerConfigService;
    @Autowired
    private MysqlConfigService mysqlConfigService;

    @RequestMapping(value = { "/pumaServerSetting" })
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询PumaServerConfig
        List<PumaServerConfig> pumaServerConfigs = pumaServerConfigService.findAll();

        map.put("pumaServerConfigs", pumaServerConfigs);
        map.put("pumaServerSettingActive", "active");
        map.put("path", "pumaServerSetting");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/pumaServerSetting/create" }, method = RequestMethod.GET)
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pumaServerSettingActive", "active");
        map.put("path", "pumaServerSetting");
        map.put("subPath", "create");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/pumaServerSetting/modify" }, method = RequestMethod.GET)
    public ModelAndView modify(String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询PumaServerConfig
        PumaServerConfig pumaServerConfig = this.pumaServerConfigService.find(new ObjectId(id));
        map.put("pumaServerConfig", pumaServerConfig);
        map.put("pumaServerSettingActive", "active");
        map.put("path", "pumaServerSetting");
        map.put("subPath", "modify");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/pumaServerSetting/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String createPost(String mysqlName, String[] host, String target) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (mysqlName == null) {
                throw new IllegalArgumentException("mysqlName不能为空");
            }
            if (host == null || host.length <= 0) {
                throw new IllegalArgumentException("host不能为空");
            }
            if (target == null) {
                throw new IllegalArgumentException("target不能为空");
            }
            //验证mysqlName是否存在
            if (mysqlConfigService.find(mysqlName) == null) {
                throw new IllegalArgumentException("不存在名称为" + mysqlName + "的数据库配置，如果需要请先添加！");
            }
            //保存
            PumaServerConfig pumaServerConfig = new PumaServerConfig();
            pumaServerConfig.setMysqlName(mysqlName);
            pumaServerConfig.setHosts(Arrays.asList(host));
            pumaServerConfig.setTarget(target);
            this.pumaServerConfigService.save(pumaServerConfig);

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

    @RequestMapping(value = { "/pumaServerSetting/modify" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String modifyPost(String id, String mysqlName, String[] host, String target) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("id不能为空");
            }
            if (mysqlName == null) {
                throw new IllegalArgumentException("mysqlName不能为空");
            }
            if (host == null || host.length <= 0) {
                throw new IllegalArgumentException("host不能为空");
            }
            if (target == null) {
                throw new IllegalArgumentException("target不能为空");
            }
            //保存
            PumaServerConfig pumaServerConfig = new PumaServerConfig();
            pumaServerConfig.setId(new ObjectId(id));
            pumaServerConfig.setMysqlName(mysqlName);
            pumaServerConfig.setHosts(Arrays.asList(host));
            pumaServerConfig.setTarget(target);
            this.pumaServerConfigService.save(pumaServerConfig);

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

    @RequestMapping(value = { "/pumaServerSetting/delete" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String delete(String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("id不能为空");
            }
            //删除
            this.pumaServerConfigService.remove(new ObjectId(id));

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
