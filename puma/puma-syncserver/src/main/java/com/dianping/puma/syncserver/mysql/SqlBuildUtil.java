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
        _ve.setProperty("class.resource.loader.modificationCheckInterval", "-1");
        _ve.setProperty("input.encoding", "UTF-8");
        _ve.init();
    }

    public static void main(String[] args) throws Exception {
        String database = "db";
        String table = "t";
        List<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("desc");
        System.out.println(buildInsertSql(database, table, columns));
        
        //test update
        columns.clear();
        columns.add("id");
        columns.add("name");
        columns.add("desc");
        List<String> whereColumns = new ArrayList<String>();
        whereColumns.add("id");
        whereColumns.add("name");
        System.out.println(buildUpdateSql(database, table, columns, whereColumns));
    }

    public static String buildInsertSql(String database, String table, List<String> columns) {
        //取得velocity的模版
        Template t = _ve.getTemplate("/sql_template/insertSql.vm");
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        context.put("database", database);
        context.put("table", table);
        context.put("columns", columns);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

    public static String buildUpdateSql(String database, String table, List<String> updateColumns, List<String> whereColumns) {
        //取得velocity的模版
        Template t = _ve.getTemplate("/sql_template/updateSql.vm");
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        context.put("database", database);
        context.put("table", table);
        context.put("updateColumns", updateColumns);
        context.put("whereColumns", whereColumns);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

    public static String buildDeleteSql(String database, String table, List<String> columns) {
        //取得velocity的模版
        Template t = _ve.getTemplate("insertSql.vm");
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        context.put("database", database);
        context.put("table", table);
        context.put("columns", columns);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

}
