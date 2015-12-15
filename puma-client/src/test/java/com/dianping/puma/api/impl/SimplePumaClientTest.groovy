package com.dianping.puma.api.impl

import com.dianping.puma.api.PumaClientConfig
import com.dianping.puma.api.PumaClientException
import com.dianping.puma.core.dto.BinlogMessage
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse
import com.dianping.puma.core.event.DdlEvent
import com.dianping.puma.core.model.BinlogInfo
import com.dianping.puma.core.util.ConvertHelper
import com.dianping.puma.core.util.constant.DdlEventSubType
import com.dianping.puma.core.util.constant.DdlEventType
import com.dianping.puma.core.util.sql.DDLType
import com.google.common.net.MediaType
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.junit.Test

import java.util.concurrent.atomic.AtomicReference

import static org.mockito.Matchers.*
import static org.mockito.Mockito.*

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
class SimplePumaClientTest {
    SimplePumaClient target = spy(new SimplePumaClient(new PumaClientConfig(enableEventLog: true)));

    @Test(expected = PumaClientException.class)
    public void testNotThreadSafe() throws Exception {
        final AtomicReference<SimplePumaClient> reference = new AtomicReference<SimplePumaClient>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                reference.set(new SimplePumaClient(new PumaClientConfig(enableEventLog: true)));
            }
        }).start();

        while (reference.get() == null) {
            Thread.sleep(10);
        }

        reference.get().get(1);
    }

    @Test
    public void testBuildGetRequest() throws Exception {
        def path = "/debug"
        def pairs = [new BasicNameValuePair("id", "1")]
        def request = target.buildRequest(path, "GET", pairs, null);
        assert request.getMethod() == "GET"
        assert request.getURI().getRawQuery() == "id=1"
        assert request.getURI().getPath() == path
    }

    @Test
    public void testBuildPostRequest() throws Exception {
        def path = "/debug"
        def pairs = [new BasicNameValuePair("id", "1")]
        def json = "{}"
        def request = target.buildRequest(path, "POST", pairs, json);
        assert request.getMethod() == "POST"
        assert request.getURI().getRawQuery() == "id=1"
        assert request.getURI().getPath() == path
        assert ((HttpPost) request).getEntity().getContent().getText() == json
    }

    @Test
    public void testDecodeJson() throws Exception {
        def response = new BinlogGetResponse(binlogMessage: new BinlogMessage(binlogEvents: []));
        def httpResponse = [
                getEntity : { new StringEntity(ConvertHelper.toJson(response)) },
                getHeaders: { null }
        ] as HttpResponse

        def result = target.decode(BinlogGetResponse.class, httpResponse)

        assert result.getBinlogMessage().getBinlogEvents() != null
    }

    @Test
    public void testDecodeRaw() throws Exception {
        def event = new DdlEvent(
                binlogInfo: new BinlogInfo(),
                ddlType: DDLType.ALTER_DATABASE,
                ddlEventType: DdlEventType.DDL_ALTER,
                ddlEventSubType: DdlEventSubType.DDL_ALTER_EVENT
        )

        def httpResponse = [
                getEntity : { new ByteArrayEntity(target.CODEC.encodeList([event])) },
                getHeaders: { [[getValue: { MediaType.OCTET_STREAM.toString() }] as Header] as Header[] }
        ] as HttpResponse

        def result = target.decode(BinlogGetResponse.class, httpResponse)

        assert result.getBinlogMessage().getBinlogEvents().size() == 1
    }

    @Test
    public void testNeedToSubscribe() throws Exception {
        target.token = null;
        assert target.needToSubscribe(BinlogGetResponse.class)
        assert !target.needToSubscribe(BinlogSubscriptionResponse.class)

        target.token = "token"
        assert !target.needToSubscribe(BinlogGetResponse.class)
        assert !target.needToSubscribe(BinlogSubscriptionResponse.class)

        target.token = null;
    }

    @Test
    public void testAddToken() throws Exception {
        def pairs = [new BasicNameValuePair("name", "xxx"), new BasicNameValuePair("token", "b")]
        target.token = "a"

        target.addToken(pairs)

        assert pairs.find { it.getName().equals("name") }
        assert pairs.find { it.getName().equals("token") && it.getValue().equals("a") }
    }

    @Test
    public void testSuccess() throws Exception {
        def client = mock(HttpClient.class);

        SimplePumaClient pumaClient = spy(new SimplePumaClient(new PumaClientConfig(enableEventLog: true), client))

        doReturn(false).when(pumaClient).needToSubscribe(any(Class.class));
        doReturn(null).when(pumaClient).buildRequest(anyString(), anyString(), anyList(), anyString())
        doReturn(new BinlogSubscriptionResponse()).when(pumaClient).decode(any(Class.class), any(HttpResponse.class))

        def httpResponse = [
                getStatusLine: { [getStatusCode: { HttpStatus.SC_OK }] as StatusLine }
        ] as HttpResponse

        doReturn(httpResponse).when(client).execute(any())

        def response = pumaClient.execute("test", null, "GET", null, BinlogSubscriptionResponse.class)

        assert response != null
        verify(client, (times(1))).execute(any())
    }

    @Test
    public void test500() throws Exception {
        def client = mock(HttpClient.class);

        SimplePumaClient pumaClient = spy(new SimplePumaClient(new PumaClientConfig(enableEventLog: true), client))

        doReturn(false).when(pumaClient).needToSubscribe(any(Class.class));
        doReturn(null).when(pumaClient).buildRequest(anyString(), anyString(), anyList(), anyString())
        doReturn(new BinlogSubscriptionResponse()).when(pumaClient).decode(any(Class.class), any(HttpResponse.class))

        def httpResponse = [
                getStatusLine: { [getStatusCode: { HttpStatus.SC_INTERNAL_SERVER_ERROR }] as StatusLine },
                getEntity    : { new StringEntity("error") },
        ] as HttpResponse

        doReturn(httpResponse).when(client).execute(any())

        try {
            pumaClient.execute("test", null, "GET", null, BinlogSubscriptionResponse.class)
            assert false //should not be here
        } catch (Exception e) {
            assert e.getMessage().contains("500")
        }

        verify(client, (times(1))).execute(any())
    }

    @Test
    public void test401() throws Exception {
        def client = mock(HttpClient.class);

        SimplePumaClient pumaClient = spy(new SimplePumaClient(new PumaClientConfig(enableEventLog: true), client))

        doNothing().when(pumaClient).addToken(anyList())
        doReturn(false).when(pumaClient).needToSubscribe(any(Class.class));
        doReturn(null).when(pumaClient).buildRequest(anyString(), anyString(), anyList(), anyString())

        final def decodeQueue = new ArrayDeque()
        decodeQueue.add(new BinlogSubscriptionResponse())
        decodeQueue.add(new BinlogGetResponse())

        doAnswer({ decodeQueue.pop() }).when(pumaClient).decode(any(Class.class), any(HttpResponse.class))

        final def responseQueue = new ArrayDeque<HttpResponse>();
        responseQueue.add([
                getStatusLine: { [getStatusCode: { HttpStatus.SC_UNAUTHORIZED }] as StatusLine },
        ] as HttpResponse);
        responseQueue.add([
                getStatusLine: { [getStatusCode: { HttpStatus.SC_OK }] as StatusLine },
        ] as HttpResponse);
        responseQueue.add([
                getStatusLine: { [getStatusCode: { HttpStatus.SC_OK }] as StatusLine },
        ] as HttpResponse);

        doAnswer({ responseQueue.pop() }).when(client).execute(any())

        def response = pumaClient.execute("test", null, "GET", null, BinlogGetResponse.class)
        assert response != null

        verify(client, times(3)).execute(any())
        verify(pumaClient, times(2)).decode(any(), any())
        verify(pumaClient, times(3)).buildRequest(anyString(), anyString(), anyList(), anyString())
        verify(pumaClient, times(1)).addToken(anyList())
        verify(pumaClient, times(3)).needToSubscribe(any())
    }
}
