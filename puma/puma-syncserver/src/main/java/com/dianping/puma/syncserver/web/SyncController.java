package com.dianping.puma.syncserver.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
public class SyncController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);

    @RequestMapping(value = "/createSync", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadShopTemplate(HttpServletRequest request, String syncXml) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //解析syncXml，得到Sync对象
            
            //启动Sync对象
            
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", "对不起，服务器内部错误。");
            LOG.error(e.getMessage(), e);
        }
        Gson gson = new Gson();
        return gson.toJson(map);

    }
}
