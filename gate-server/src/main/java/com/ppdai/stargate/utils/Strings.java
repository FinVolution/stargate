package com.ppdai.stargate.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenlang on 2020/8/31
 **/
public final class Strings {
    private Strings() {
        throw new AssertionError();
    }

    public static <T> String toStringIfNotEmpty(T t, String defaultStr) {
        if (t == null || "".equals(t)) return defaultStr;
        return t.toString();
    }

    public static JSONObject stringToJsonObject(String str) {
        if (StringUtils.isBlank(str)) return new JSONObject();
        return JSONObject.parseObject(str);
    }

    public static <T> List<T> stringToList(String str, Class<T> clazz) {
        if (StringUtils.isBlank(str)) new ArrayList<>();
        return JSONObject.parseArray(str, clazz);
    }

}
