package com.dianping.puma.extensions.notify;

import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.service.PumaWeChatService;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by xiaotian.li on 16/3/28.
 * Email: lixiaotian07@gmail.com
 */
public class PaasWeChatService implements PumaWeChatService {

    private String httpPath = "http://web.paas.dp/wechat/sendByRequestBody";

    private HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(
                    RequestConfig.custom()
                            .setConnectTimeout(10 * 1000)
                            .setSocketTimeout(10 * 60 * 1000)
                            .build()).build();

    @Override
    public void send(String recipient, String message) {
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

        } catch (Throwable t) {
            throw new IllegalArgumentException("Send wechat error.", t);
        }
    }
}
