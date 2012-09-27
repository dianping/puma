/**
 * Project: WorkPlatform.web File Created at 2009-9-27 $Id: 2009-9-27 17:54:43 Galaxy Team $ Copyright 2008 Alibaba.com
 * Corporation Limited. All rights reserved. This software is the confidential and proprietary information of Alibaba
 * Company. ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */

package com.dianping.puma.core.sync;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class DefinitionXmlParser {

    private static Digester digester = new Digester();

    static {
        // definition
        digester.addObjectCreate("sync", Sync.class);

        // definition/selector
        digester.addObjectCreate("sync/from", Config.class);
        digester.addSetNext("sync/from", "setFrom");
        digester.addCallMethod("sync/from/host", "setHost", 0);
        digester.addCallMethod("sync/from/port", "setPort", 0);
        digester.addCallMethod("sync/from/username", "setUsername", 0);
        digester.addCallMethod("sync/from/password", "setPassword", 0);

    }

    public static synchronized Sync parse(String define) throws IOException, SAXException {
        try {
            Sync definition = new Sync();
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

    public static void main(String[] args) {
        new File("/home/wukezhu/document/mywork/puma/puma/puma-core/src/main/java/com/dianping/puma/core/sync/sync.xml");
    }
}
