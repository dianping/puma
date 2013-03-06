package com.dianping.puma.admin.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.dianping.puma.admin.service.MysqlConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.MysqlHost;

@Controller
public class MysqlSettingController {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlSettingController.class);
    @Autowired
    private MysqlConfigService mysqlConfigService;

    @RequestMapping(value = { "/mysqlSetting" })
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        //        System.out.println(syncConfigService.find());
        //查询MysqlConfig
        List<MysqlConfig> mysqlConfigs = mysqlConfigService.findAll();

        map.put("mysqlConfigs", mysqlConfigs);
        map.put("mysqlSettingActive", "active");
        map.put("path", "mysqlSetting");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/mysqlSetting/create" }, method = RequestMethod.GET)
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("mysqlSettingActive", "active");
        map.put("path", "mysqlSetting");
        map.put("subPath", "create");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/mysqlSetting/modify" }, method = RequestMethod.GET)
    public ModelAndView modify(String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        //查询MysqlConfig
        MysqlConfig mysqlConfig = this.mysqlConfigService.find(new ObjectId(id));
        map.put("mysqlConfig", mysqlConfig);
        map.put("mysqlSettingActive", "active");
        map.put("path", "mysqlSetting");
        map.put("subPath", "modify");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = { "/mysqlSetting/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String createPost(String name, Long[] serverId, String[] host, String[] username, String[] password) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (name == null) {
                throw new IllegalArgumentException("name不能为空");
            }
            if (serverId == null || serverId.length <= 0) {
                throw new IllegalArgumentException("serverId不能为空");
            }
            if (host == null || host.length <= 0) {
                throw new IllegalArgumentException("host不能为空");
            }
            if (username == null || username.length <= 0) {
                throw new IllegalArgumentException("username不能为空");
            }
            if (password == null || password.length <= 0) {
                throw new IllegalArgumentException("password不能为空");
            }
            if (username.length != serverId.length || username.length != host.length || username.length != password.length) {
                throw new IllegalArgumentException("serverId,host,suername,password数量必须一致");
            }
            //保存
            MysqlConfig mysqlConfig = new MysqlConfig();
            mysqlConfig.setName(name);
            List<MysqlHost> hosts = new ArrayList<MysqlHost>(host.length);
            for (int i = 0; i < username.length; i++) {
                MysqlHost mysqlHost = new MysqlHost();
                mysqlHost.setServerId(serverId[i]);
                mysqlHost.setHost(host[i]);
                mysqlHost.setUsername(username[i]);
                mysqlHost.setPassword(password[i]);
                if (mysqlHost.getServerId() > 0 && StringUtils.isNotBlank(mysqlHost.getHost())
                        && StringUtils.isNotBlank(mysqlHost.getUsername())) {
                    hosts.add(mysqlHost);
                } else {
                    throw new IllegalArgumentException("serverId,host,uername都不能为空!serverId必须大于0！");
                }
            }
            mysqlConfig.setHosts(hosts);
            this.mysqlConfigService.save(mysqlConfig);

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

    @RequestMapping(value = { "/mysqlSetting/modify" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String modifyPost(String id, String name, Long[] serverId, String[] host, String[] username, String[] password) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("id不能为空");
            }
            if (name == null) {
                throw new IllegalArgumentException("name不能为空");
            }
            if (serverId == null || serverId.length <= 0) {
                throw new IllegalArgumentException("serverId不能为空");
            }
            if (host == null || host.length <= 0) {
                throw new IllegalArgumentException("host不能为空");
            }
            if (username == null || username.length <= 0) {
                throw new IllegalArgumentException("username不能为空");
            }
            if (password == null || password.length <= 0) {
                throw new IllegalArgumentException("password不能为空");
            }
            if (username.length != serverId.length || username.length != host.length || username.length != password.length) {
                throw new IllegalArgumentException("serverId,host,suername,password数量必须一致");
            }
            //保存
            MysqlConfig mysqlConfig = new MysqlConfig();
            mysqlConfig.setId(new ObjectId(id));
            mysqlConfig.setName(name);
            List<MysqlHost> hosts = new ArrayList<MysqlHost>(host.length);
            for (int i = 0; i < username.length; i++) {
                MysqlHost mysqlHost = new MysqlHost();
                mysqlHost.setServerId(serverId[i]);
                mysqlHost.setHost(host[i]);
                mysqlHost.setUsername(username[i]);
                mysqlHost.setPassword(password[i]);
                if (mysqlHost.getServerId() > 0 && StringUtils.isNotBlank(mysqlHost.getHost())
                        && StringUtils.isNotBlank(mysqlHost.getUsername())) {
                    hosts.add(mysqlHost);
                } else {
                    throw new IllegalArgumentException("serverId,host,uername都不能为空!serverId必须大于0！");
                }
            }
            mysqlConfig.setHosts(hosts);
            this.mysqlConfigService.save(mysqlConfig);

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

    @RequestMapping(value = { "/mysqlSetting/delete" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String delete(String id) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("id不能为空");
            }
            //删除
            this.mysqlConfigService.remove(new ObjectId(id));

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
