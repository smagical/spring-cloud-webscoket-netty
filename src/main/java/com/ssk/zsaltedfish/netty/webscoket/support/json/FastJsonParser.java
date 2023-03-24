package com.ssk.zsaltedfish.netty.webscoket.support.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;

public class FastJsonParser implements JsonParser {

    @Override
    public boolean validate(String json) {
        return JSONValidator.from(json).validate();
    }

    @Override
    public <T> T getObject(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    @Override
    public String toJSON(Object obj) {
        return JSON.toJSONString(obj);
    }
}
