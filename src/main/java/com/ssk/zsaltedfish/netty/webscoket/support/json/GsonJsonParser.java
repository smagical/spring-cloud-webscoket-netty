package com.ssk.zsaltedfish.netty.webscoket.support.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class GsonJsonParser implements JsonParser {
    @Override
    public boolean validate(String json) {
        JsonElement jsonElement;
        try {
            jsonElement = com.google.gson.JsonParser.parseString(json);
        } catch (Exception e) {
            return false;
        }
        if (jsonElement == null) {
            return false;
        }
        return true;
    }

    @Override
    public <T> T getObject(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }

    @Override
    public String toJSON(Object obj) {
        return new Gson().toJson(obj);
    }
}
