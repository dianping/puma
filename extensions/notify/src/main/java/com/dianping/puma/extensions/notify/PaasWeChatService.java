package com.dianping.puma.extensions.notify;

import com.dianping.puma.alarm.core.monitor.notify.service.PumaWeChatService;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.VersionInfo;

/**
 * Created by xiaotian.li on 16/3/28.
 * Email: lixiaotian07@gmail.com
 */
public class PaasWeChatService implements PumaWeChatService {

    private String httpPath = "http://web.paas.dp/wechat/sendByRequestBody";

    private static final int TIMEOUT = 2000;

    private static final int CONNECTION_TIMEOUT = 2000;

    private static final int SOCKET_TIMEOUT = 1000;

    private HttpClient httpClient;

    private void createHttpClient() {
        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        final VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.http.client", getClass().getClassLoader());
        final String release = (vi != null) ? vi.getRelease() : VersionInfo.UNAVAILABLE;
        HttpProtocolParams.setUserAgent(params, "Apache-HttpClient/" + release + " (java 1.5)");

        // 等待获取链接时间
        ConnManagerParams.setTimeout(params, TIMEOUT);
        // 链接超时时间
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        // 读取超时时间
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(connectionManager, params);
    }

    @Override
    public void send(String recipient, String message) {
        if (httpClient == null) {
            createHttpClient();
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("keyword", recipient);
        jsonObject.addProperty("content", message);

        try {
            HttpPost httpPost = new HttpPost(httpPath);
            httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.addHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new PumaAlarmNotifyException();
            }

            EntityUtils.consumeQuietly(httpResponse.getEntity());

        } catch (Throwable t) {
            throw new IllegalArgumentException("Send wechat error.", t);
        }
    }
}
