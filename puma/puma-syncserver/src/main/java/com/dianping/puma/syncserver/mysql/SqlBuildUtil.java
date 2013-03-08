package com.dianping.puma.syncserver.mysql;

import java.io.StringWriter;

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
        _ve.setProperty("class.resource.loader.cache", true);
        _ve.setProperty("class.resource.loader.modificationCheckInterval", "-1");
        _ve.setProperty("input.encoding", "UTF-8");
        _ve.setProperty("runtime.log", "/tmp/velocity.log");
        _ve.init();
    }

    public static String buildDeleteSql(RowChangedEvent event) {
        return buildSql(event, "/sql_template/deleteSql.vm");
    }

    public static String buildUpdateSql(RowChangedEvent event) {
        return buildSql(event, "/sql_template/updateSql.vm");
    }

    public static String buildReplaceSql(RowChangedEvent event) {
        return buildSql(event, "/sql_template/replaceSql.vm");
    }

    public static String buildUpdateToNullSql(RowChangedEvent event) {
        return buildSql(event, "/sql_template/updateToNullSql.vm");
    }

    public static String buildInsertSql(RowChangedEvent event) {
        return buildSql(event, "/sql_template/insertSql.vm");
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

}
