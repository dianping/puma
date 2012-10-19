package com.dianping.puma.syncserver.mysql;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.dianping.puma.core.event.RowChangedEvent;

/**
 * @author wukezhu
 */
public class SqlBuildUtil {

    private static final VelocityEngine _ve;
    static {
        _ve = new VelocityEngine();
        _ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
        _ve.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        _ve.setProperty("class.resource.loader.cache", false);
        _ve.setProperty("class.resource.loader.modificationCheckInterval", "-1");
        _ve.setProperty("input.encoding", "UTF-8");
        _ve.init();
    }

    public static void main(String[] args) throws Exception {
        //test insert
        String database = "db";
        String table = "t";
        List<String> columns = new ArrayList<String>();
        columns.add("id");
        columns.add("name");
        columns.add("desc");
        System.out.println(buildInsertSql(database, table, columns));
        System.out.println("-----------------------------");
        //test update
        columns.clear();
        columns.add("id");
        columns.add("name");
        columns.add("desc");
        List<String> whereColumns = new ArrayList<String>();
        whereColumns.add("id");
        whereColumns.add("name");
        System.out.println(buildUpdateSql(database, table, columns, whereColumns));
        System.out.println("-----------------------------");
        //test delete
        List<String> whereColumns2 = new ArrayList<String>();
        whereColumns2.add("id");
        whereColumns2.add("name");
        System.out.println(buildDeleteSql(database, table, whereColumns2));
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

    public static String buildDeleteSql(String database, String table, List<String> whereColumns) {
        //取得velocity的模版
        Template t = _ve.getTemplate("/sql_template/deleteSql.vm");
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        context.put("database", database);
        context.put("table", table);
        context.put("whereColumns", whereColumns);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

    public static String buildReplaceSql(String database, String table, List<String> whereColumns) {
        //取得velocity的模版
        Template t = _ve.getTemplate("/sql_template/replaceSql.vm");
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        context.put("database", database);
        context.put("table", table);
        context.put("whereColumns", whereColumns);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

    public static String buildUpdateToNullSql(String database, String table, List<String> updateColumns, List<String> whereColumns) {
        //取得velocity的模版
        Template t = _ve.getTemplate("/sql_template/updateToNullSql.vm");
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

    public static String buildDeleteSql2(RowChangedEvent event) {
        return buildSql(event, "/sql_template2/deleteSql.vm");
    }

    public static String buildUpdateSql2(RowChangedEvent event) {
        return buildSql(event, "/sql_template2/updateSql.vm");
    }

    public static String buildReplaceSql2(RowChangedEvent event) {
        return buildSql(event, "/sql_template2/replaceSql.vm");
    }

    public static String buildUpdateToNullSql2(RowChangedEvent event) {
        return buildSql(event, "/sql_template2/updateToNullSql.vm");
    }

    public static String buildInsertSql2(RowChangedEvent event) {
        return buildSql(event, "/sql_template2/insertSql.vm");
    }

    private static String buildSql(RowChangedEvent event, String file) {
        //取得velocity的模版
        Template t = _ve.getTemplate(file);
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //把数据填入上下文
        context.put("event", event);
        //输出流
        StringWriter writer = new StringWriter();
        //转换输出
        t.merge(context, writer);
        return writer.toString();
    }

    //    public boolean needQuot(Class cls){
    //        
    //    }

}
