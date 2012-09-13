package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class AdminController {
   @RequestMapping(value = "/")
   public ModelAndView allApps(HttpServletRequest request, HttpServletResponse response) {
      Map<String, Object> map = new HashMap<String, Object>();
      return new ModelAndView("index", map);
   }
}
