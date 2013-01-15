package com.dianping.puma.admin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.model.mapping.ColumnMapping;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;

public class SyncXmlParser {

    private static Digester digester = new Digester();

    static {

        // sync/instance
//        digester.addObjectCreate("sync/instance", MysqlMapping.class);//创建对象
        //        digester.addSetNext("sync/instance", "setInstance");//添加到父亲
//        digester.addSetProperties("sync/instance");//tag的attr

        // sync/instance/database
        digester.addObjectCreate("sync/instance/database", DatabaseMapping.class);
        digester.addSetNext("sync/instance/database", "addDatabase");
        digester.addSetProperties("sync/instance/database");//tag的attr

        // sync/instance/database/table
        digester.addObjectCreate("sync/instance/database/table", TableMapping.class);
        digester.addSetNext("sync/instance/database/table", "addTable");
        digester.addSetProperties("sync/instance/database/table");//tag的attr

        // sync/instance/database/table/colmn
        digester.addObjectCreate("sync/instance/database/table/column", ColumnMapping.class);
        digester.addSetNext("sync/instance/database/table/column", "addColumn");
        digester.addSetProperties("sync/instance/database/table/column");//tag的attr
    }

    public static synchronized SyncConfig parse(String define) throws IOException, SAXException {
        try {
            SyncConfig definition = new SyncConfig();
            digester.push(definition);
            digester.parse(new StringReader(define));
            return definition;
        } finally {
            digester.clear();
        }
    }

    public static synchronized MysqlMapping parse2(String define) throws IOException, SAXException {
        try {
            MysqlMapping definition = new MysqlMapping();
            digester.push(definition);
            digester.parse(new StringReader(define));
            return definition;
        } finally {
            digester.clear();
        }
    }

    //    private static Validator validator = null;
    //    static {
    //        // 创立验证xml文件的schema
    //        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    //        URL url = DefinitionXmlParser.class.getResource("/com/alibaba/asc/edison/bean/buckettest_schema.xsd");
    //        Schema schema = null;
    //        try {
    //            schema = schemaFactory.newSchema(url);
    //        } catch (SAXException e) {
    //            throw new RuntimeException(e);
    //        }
    //        validator = schema.newValidator();
    //    }

    //    public static void validate(String define) throws SAXException, IOException {
    //        // 得到验证的数据源
    //        Source source = new StreamSource(new StringReader(define));
    //        // 验证
    //        validator.validate(source);
    //    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SAXException {
        File file = new File("/home/wukezhu/document/mywork/puma/puma/puma-admin/src/main/resources/sync2.xml");
        System.out.println(parse2(IOUtils.toString(new FileInputStream(file), "UTF-8")));
    }
}
