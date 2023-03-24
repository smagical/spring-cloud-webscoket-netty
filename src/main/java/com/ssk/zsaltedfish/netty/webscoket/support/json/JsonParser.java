package com.ssk.zsaltedfish.netty.webscoket.support.json;

public interface JsonParser {
    public boolean validate(String json);

    public <T> T getObject(String json, Class<T> clazz);

    public String toJSON(Object obj);
}
