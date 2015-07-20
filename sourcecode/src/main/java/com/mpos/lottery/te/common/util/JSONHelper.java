package com.mpos.lottery.te.common.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.Map;

public class JSONHelper {

    public static Object decode(String json) {
        return JSONValue.parse(json);
    }

    public static String encode(Object[] input) {
        JSONArray array = new JSONArray();
        for (Object o : input) {
            array.add(o);
        }
        return JSONValue.toJSONString(array);
    }

    public static String encode(List input) {
        JSONArray array = new JSONArray();
        for (Object o : input) {
            array.add(o);
        }
        return JSONValue.toJSONString(array);
    }

    public static String encode(Map input) {
        return JSONValue.toJSONString(input);
    }
}
