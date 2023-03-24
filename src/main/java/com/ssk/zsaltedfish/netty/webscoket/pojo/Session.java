package com.ssk.zsaltedfish.netty.webscoket.pojo;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AttributeKey;

public interface Session {
    public final static AttributeKey<HttpHeaders> WEB_SOCKET_HEADERS_KEY =
            AttributeKey.newInstance("WEB_SOCKET_HEADERS_KEY");
    public final static AttributeKey<String> WEB_SOCKET_URI_KEY =
            AttributeKey.newInstance("WEB_SOCKET_URI_KEY");
    public final static AttributeKey<String> WEB_SOCKET_ENDPOINT_PATH_KEY =
            AttributeKey.newInstance("WEB_SOCKET_ENDPOINT_PATH_KEY");

    void write(Object... messages);

    void flush();

    void writeAndFlush(Object... messages);

    void close();

    boolean isClosed();

    <T> void setAttribute(AttributeKey name, T value);

    <T> T getAttribute(AttributeKey<T> name);

    <T> T removeAttribute(AttributeKey<T> name);
}
