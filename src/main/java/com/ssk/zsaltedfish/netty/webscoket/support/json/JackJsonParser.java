package com.ssk.zsaltedfish.netty.webscoket.support.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JackJsonParser implements JsonParser {

    @Override
    public boolean validate(String json) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> T getObject(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toJSON(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
