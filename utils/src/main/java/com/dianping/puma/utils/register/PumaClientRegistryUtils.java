package com.dianping.puma.utils.register;

import com.dianping.lion.client.ConfigCache;
import com.dianping.puma.utils.exception.PumaClientRegisterException;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientRegistryUtils {

    private static final String PUMA_UTILS_CLIENT_REGISTRY_KEY = "puma.utils.clientRegistry";

    private static HttpClient httpClient = HttpClients.createDefault();

    private static ConfigCache configCache = ConfigCache.getInstance();

    public static void register(PumaClientRegistryEntity entity) throws PumaClientRegisterException {
        String clientName = entity.getClientName();
        if (StringUtils.isBlank(clientName)) {
            throw new PumaClientRegisterException("Client name can not be null.");
        }

        if (!exists(clientName)) {
            register0(entity);
        }
    }

    public static boolean exists(String clientName) throws PumaClientRegisterException {
        String uri = configCache.getProperty(PUMA_UTILS_CLIENT_REGISTRY_KEY);
        if (StringUtils.isBlank(uri)) {
            throw new PumaClientRegisterException("Failed to find client register uri, " +
                    "please contact administrator.");
        }

        HttpGet httpGet = new HttpGet(uri + "/" + clientName);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new PumaClientRegisterException("Failed to register puma client, status code[%s].", statusCode);
            }

            String json = EntityUtils.toString(response.getEntity());
            PumaClientRegistryEntity entity = new Gson().fromJson(json, PumaClientRegistryEntity.class);
            return entity != null;

        } catch (IOException e) {
            throw new PumaClientRegisterException("Failed to register puma client.", e);
        }
    }

    public static void register0(PumaClientRegistryEntity entity) throws PumaClientRegisterException {
        String groupName = entity.getGroupName();
        if (StringUtils.isBlank(groupName)) {
            entity.setGroupName("Uncatalogued");
        }

        String uri = configCache.getProperty(PUMA_UTILS_CLIENT_REGISTRY_KEY);
        if (StringUtils.isBlank(uri)) {
            throw new PumaClientRegisterException("Failed to find client register uri, " +
                    "please contact administrator.");
        }

        HttpPost httpPost = new HttpPost(uri);
        String json = new Gson().toJson(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(json, "utf-8"));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new PumaClientRegisterException("Failed to register puma client, status code[%s]", statusCode);
            }

        } catch (IOException e) {
            throw new PumaClientRegisterException("Failed to register puma client.", e);
        }
    }
}
