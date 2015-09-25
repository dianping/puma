package com.dianping.puma.admin.web;

import com.dianping.puma.admin.model.CheckTaskModel;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.model.CheckTaskQueryModel;
import com.dianping.puma.biz.model.PageModel;
import com.dianping.puma.biz.service.CheckTaskService;
import com.dianping.puma.core.util.GsonUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.util.Eval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Controller
@RequestMapping(value = {"/puma-check"})
public class PumaCheckController extends BasicController {

    private static final String CLASS_NAME = "className";
    private static final String IT = "it";
    private static final int PAGE_SIZE = 200;
    private static final Pattern TEMPLATE_REGEX = Pattern.compile(".*\\$\\{.+\\}.*");
    private final GStringTemplateEngine engine = new GStringTemplateEngine();

    @Autowired
    private CheckTaskService checkTaskService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object list(CheckTaskQueryModel queryModel, Integer page) {
        Map<String, Object> result = new HashMap<String, Object>();
        PageModel pageModel = new PageModel(page == null ? 1 : page.intValue(), PAGE_SIZE);
        result.put("list", checkTaskService.list(queryModel, pageModel));
        result.put("page", pageModel);
        return result;
    }

    @RequestMapping(method = RequestMethod.GET, value = "{taskName}")
    @ResponseBody
    public Object get(@PathVariable String taskName) {

        CheckTaskEntity entity = checkTaskService.findByName(taskName);
        if (entity == null) {
            return null;
        }

        CheckTaskModel model = new CheckTaskModel();
        model.setSourceDsBuilderProp(convertJsonToMap(entity.getSourceDsBuilder(), entity.getSourceDsBuilderProp()));
        model.setTargetDsBuilderProp(convertJsonToMap(entity.getTargetDsBuilder(), entity.getTargetDsBuilderProp()));

        model.setSourceFetcherProp(convertJsonToMap(entity.getSourceFetcher(), entity.getSourceFetcherProp()));
        model.setTargetFetcherProp(convertJsonToMap(entity.getTargetFetcher(), entity.getTargetFetcherProp()));

        model.setMapperProp(convertJsonToMap(entity.getMapper(), entity.getMapperProp()));
        model.setComparisonProp(convertJsonToMap(entity.getComparison(), entity.getComparisonProp()));

        CheckTaskModel.BaseInfo baseInfo = new CheckTaskModel.BaseInfo();
        baseInfo.setCursor(entity.getCursor());
        baseInfo.setName(entity.getTaskName());
        model.setBaseInfo(baseInfo);

        return model;
    }

    private Map<String, Object> convertJsonToMap(String className, String prop) {
        Map<String, Object> map = GsonUtil.fromJson(prop, new TypeToken<Map<String, Object>>() {
        }.getType());
        map.put(CLASS_NAME, className);
        return map;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{taskName}")
    @ResponseBody
    public Object remove(@PathVariable String taskName) {
        checkTaskService.deleteByTaskName(taskName);
        return null;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object create(@RequestBody CheckTaskModel model) throws IOException, ClassNotFoundException {

        Iterable<Object> batch = Lists.<Object>newArrayList("");

        if (!Strings.isNullOrEmpty(model.getBaseInfo().getBatch())) {
            Eval groovy = new Eval();
            batch = (Iterable<Object>) groovy.me(model.getBaseInfo().getBatch());
        }

        Map<String, Template> templateCache = new HashMap<String, Template>();

        for (Object it : batch) {
            CheckTaskEntity entity = new CheckTaskEntity();

            entity.setCursor(model.getBaseInfo().getCursor());
            entity.setTaskGroupName(model.getBaseInfo().getName());
            if (Strings.isNullOrEmpty(it.toString())) {
                entity.setTaskName(model.getBaseInfo().getName());
            } else {
                entity.setTaskName(model.getBaseInfo().getName() + "_" + it.toString());
            }

            entity.setSourceDsBuilder(model.getSourceDsBuilderProp().get(CLASS_NAME).toString());
            entity.setSourceDsBuilderProp(templateMake(templateCache, model.getSourceDsBuilderProp(), it));

            entity.setSourceFetcher(model.getSourceFetcherProp().get(CLASS_NAME).toString());
            entity.setSourceFetcherProp(templateMake(templateCache, model.getSourceFetcherProp(), it));

            entity.setTargetDsBuilder(model.getTargetDsBuilderProp().get(CLASS_NAME).toString());
            entity.setTargetDsBuilderProp(templateMake(templateCache, model.getTargetDsBuilderProp(), it));

            entity.setTargetFetcher(model.getTargetFetcherProp().get(CLASS_NAME).toString());
            entity.setTargetFetcherProp(templateMake(templateCache, model.getTargetFetcherProp(), it));

            entity.setComparison(model.getComparisonProp().get(CLASS_NAME).toString());
            entity.setComparisonProp(templateMake(templateCache, model.getComparisonProp(), it));

            entity.setMapper(model.getMapperProp().get(CLASS_NAME).toString());
            entity.setMapperProp(templateMake(templateCache, model.getMapperProp(), it));

            entity.setSuccess(true);

            checkTaskService.deleteByTaskName(entity.getTaskName());
            checkTaskService.create(entity);
        }

        return null;
    }

    protected String templateMake(Map<String, Template> templateCache, Map<String, Object> properties, Object value) throws IOException, ClassNotFoundException {

        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (CLASS_NAME.contains(entry.getKey())) {
                continue;
            }

            if (!TEMPLATE_REGEX.matcher(entry.getValue().toString()).matches()) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                String templateStr = entry.getValue().toString();
                if (!templateCache.containsKey(templateStr)) {
                    templateCache.put(templateStr, engine.createTemplate(templateStr));
                }

                Template t = templateCache.get(templateStr);
                Map<String, Object> args = new HashMap<String, Object>();
                args.put(IT, value);
                result.put(entry.getKey(), t.make(args).toString());
            }
        }
        return GsonUtil.toJson(result);
    }
}
