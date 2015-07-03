package com.dianping.puma.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;

/**
 * Dozer @ 7/2/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SinglePumaConnectorTest {
    @Test
    @Ignore
    public void testConnect() throws Exception {
//        SinglePumaConnector connector = new SinglePumaConnector("test", "www.dozer.cc", 80);
//        connector.connect();
//        connector.subscribe(true, true, true, "test", "user1", "user2");
//        System.in.read();
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.socket.timeout", 60 * 1000);

        while (true) {
            try {
                HttpResponse response1 = client.execute(new HttpGet(URI.create("http://localhost:4040/puma/binlog/get?clientName=a&database=user&table=user")));
                HttpResponse response2 = client.execute(new HttpGet(URI.create("http://localhost:4040/puma/binlog/ack?xx")));
                //todo: process
            } catch (Exception e) {
                HttpResponse response2 = client.execute(new HttpGet(URI.create("http://localhost:4040/puma/binlog/rollback?xx")));
                //todo: ha
            }
        }


    }

//    class BasicClientConnectionManagerExt extends BasicClientConnectionManager {
//        @Override
//        protected ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {
//            return super.createConnectionOperator(schreg);
//        }
//
//        @Override
//        public void releaseConnection(ManagedClientConnection conn, long keepalive, TimeUnit tunit) {
//            super.releaseConnection(conn, keepalive, tunit);
//        }
//    }
}