package com.dianping.puma.admin.web;

import com.dianping.puma.admin.model.CheckTaskModel;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.core.util.GsonUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.util.Eval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Controller
@RequestMapping(value = {"/puma-check"})
public class PumaCheckController {

    private static final String CLASS_NAME = "className";
    private static final String IT = "it";
    private final GStringTemplateEngine engine = new GStringTemplateEngine();

    @Autowired
    private CheckTaskService checkTaskService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object create(@RequestBody CheckTaskModel model) throws IOException, ClassNotFoundException {

        Iterable<Object> batch = Lists.newArrayList(new Object());

        if (!Strings.isNullOrEmpty(model.getBaseInfo().getBatch())) {
            Eval groovy = new Eval();
            batch = (Iterable<Object>) groovy.me(model.getBaseInfo().getBatch());
        }

        List<CheckTaskEntity> result = new ArrayList<CheckTaskEntity>();

        for (Object it : batch) {
            CheckTaskEntity entity = new CheckTaskEntity();

            entity.setInitTime(new Date(model.getBaseInfo().getInitTime()));

            entity.setSourceDsBuilder(model.getSourceDsBuilderProp().get(CLASS_NAME).toString());
            entity.setSourceDsBuilderProp(templateMake(model.getSourceDsBuilderProp(), it));

            entity.setSourceFetcher(model.getSourceFetcherProp().get(CLASS_NAME).toString());
            entity.setSourceFetcherProp(templateMake(model.getSourceFetcherProp(), it));

            entity.setTargetDsBuilder(model.getTargetDsBuilderProp().get(CLASS_NAME).toString());
            entity.setTargetDsBuilderProp(templateMake(model.getTargetDsBuilderProp(), it));

            entity.setTargetFetcher(model.getTargetFetcherProp().get(CLASS_NAME).toString());
            entity.setTargetFetcherProp(templateMake(model.getTargetFetcherProp(), it));

            entity.setComparison(model.getComparisonProp().get(CLASS_NAME).toString());
            entity.setComparisonProp(templateMake(model.getComparisonProp(), it));

            entity.setMapper(model.getMapperProp().get(CLASS_NAME).toString());
            entity.setMapperProp(templateMake(model.getMapperProp(), it));

            result.add(entity);
        }

        return null;
    }

    protected String templateMake(Map<String, Object> properties, Object value) throws IOException, ClassNotFoundException {

        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Template t = engine.createTemplate(entry.getValue().toString());
            Map<String, Object> args = new HashMap<String, Object>();
            args.put(IT, value);
            result.put(entry.getKey(), t.make(args).toString());
        }
        return GsonUtil.toJson(result);
    }
}
