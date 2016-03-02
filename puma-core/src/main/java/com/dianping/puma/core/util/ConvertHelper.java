package com.dianping.puma.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Created by Dozer on 11/21/14.
 */
public final class ConvertHelper {

    private final static Gson gson = new Gson();

    private final static Charset charset = Charset.forName("UTF-8");

    public static String toJson(Object item) {
        return gson.toJson(item);
    }

    public static byte[] toBytes(Object item) {
        if (item instanceof String) {
            return ((String) item).getBytes(charset);
        } else {
            return toJson(item).getBytes(charset);
        }
    }

    public static String bytesToStr(byte[] data, int index, int length) {
        if (data == null) {
            return null;
        }
        return new String(data, index, length, charset);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException exp) {
            throw new JsonSyntaxException(json, exp);
        }
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException exp) {
            throw new JsonSyntaxException(json, exp);
        }
    }

    public static <T> T fromBytes(byte[] data, int index, int length, Class<T> clazz) {
        return fromJson(bytesToStr(data, index, length), clazz);
    }

    public static <T> T fromBytes(byte[] data, int index, int length, Type type) {
        return fromJson(bytesToStr(data, index, length), type);
    }

    public static ByteBuf toPooledByteBuf(byte[] data) {
        if (data == null) {
            return null;
        }
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(data.length);
        byteBuf.writeBytes(data);
        return byteBuf;
    }
}
