package com.dianping.puma.portal.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    /**
     * 首页，使用说明
     */
    @RequestMapping(value = { "/" })
    public RedirectView readme(HttpServletRequest request, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("readmeActive", "active");
        map.put("path", "readme");
        return new RedirectView(request.getContextPath() + "/puma-monitor");
    }

    @RequestMapping(value = { "/delete" })
    public ModelAndView delete(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("deleteActive", "active");
        map.put("path", "delete");
        return new ModelAndView("main/container", map);
    }

}
