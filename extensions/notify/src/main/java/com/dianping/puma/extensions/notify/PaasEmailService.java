package com.dianping.puma.extensions.notify;

import com.dianping.puma.alarm.core.monitor.notify.service.PumaEmailService;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
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
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PaasEmailService implements PumaEmailService {

    private String httpPath = "http://web.paas.dp/mail/send";

    private HttpClient httpClient = HttpClients.createDefault();

    @Override
    public void send(String recipient, String title, String content) {
        List<NameValuePair> nameValuePairs = Lists.newArrayList();
        nameValuePairs.add(new BasicNameValuePair("recipients", recipient));
        nameValuePairs.add(new BasicNameValuePair("title", title));
        nameValuePairs.add(new BasicNameValuePair("body", content));

        HttpPost httpPost = new HttpPost(httpPath);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new PumaAlarmNotifyException();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
