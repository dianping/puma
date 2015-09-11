package com.dianping.puma.core.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class GsonUtil {

    public static final Gson gson = new Gson();

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    
    public static Object fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
