package com.ssk.zsaltedfish.netty.webscoket.support.json;

/**
 * json转换bean
 */
public interface JsonParser {
    boolean validate(String json);

    <T> T getObject(String json, Class<T> clazz);

    String toJSON(Object obj);
}
