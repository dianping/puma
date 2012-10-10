package com.dianping.puma.syncserver.mysql;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

/**
 * @author wukezhu
 */
public class SqlBuildUtil {

    private static final VelocityEngine _ve;
    static {
        _ve = new VelocityEngine();
        _ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
        _ve.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        _ve.setProperty("class.resource.loader.cache", true);
        _ve.setProperty("class.resource.loader.modificationCheckInterval", "3600");
        _ve.setProperty("input.encoding", "UTF-8");
        _ve.init();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws Exception {
        System.out.println(buildInsertSql());
    }
    
    public static String buildInsertSql(){
      //取得velocity的模版
        Template t = _ve.getTemplate("insertSql.vm");
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        List temp = new ArrayList();
        temp.add("1");
        temp.add("2");
        context.put("columns", temp);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

}
