package com.dianping.puma.test.alarm;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PumaRemoteAlarmIntegrationTest {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext(
                "classpath:spring/alarm/remote.xml",
                "classpath:spring/db/appcontext-dao.xml",
                "classpath:spring/db/appcontext-db.xml"
        );

        System.in.read();
    }
}
