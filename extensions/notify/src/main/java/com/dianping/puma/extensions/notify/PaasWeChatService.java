package com.dianping.puma.extensions.notify;

import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.service.PumaWeChatService;
import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by xiaotian.li on 16/3/28.
 * Email: lixiaotian07@gmail.com
 */
public class PaasWeChatService implements PumaWeChatService {

    private String httpPath = "http://web.paas.dp/wechat/sendByRequestBody";

    private HttpClient httpClient = HttpClients.createDefault();

    @Override
    public void send(String recipient, String message) {
        List<NameValuePair> nameValuePairs = Lists.newArrayList();
        nameValuePairs.add(new BasicNameValuePair("recipients", recipient));
        nameValuePairs.add(new BasicNameValuePair("content", message));

        HttpPost httpPost = new HttpPost(httpPath);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new PumaAlarmNotifyException();
            }

        } catch (Throwable t) {
            throw new IllegalArgumentException("Send wechat error.");
        }
    }
}
