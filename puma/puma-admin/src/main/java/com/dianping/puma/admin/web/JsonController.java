package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.puma.admin.bo.DataSource;
import com.dianping.puma.admin.util.GsonUtil;

/**
 * @author wukezhu
 */
@Controller
@RequestMapping(method = RequestMethod.POST, produces = "application/json; charset=utf-8")
public class JsonController {
    private static final Logger LOG = LoggerFactory.getLogger(JsonController.class);

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";

    @RequestMapping(value = "/loadDataSource", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadCategory(HttpSession session, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Set<DataSource> dataSources = new HashSet<DataSource>();
            //加载

            map.put("dataSources", dataSources);
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
