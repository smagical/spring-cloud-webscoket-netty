package com.ssk.zsaltedfish.netty.webscoket.pojo;

import com.ssk.zsaltedfish.netty.webscoket.server.handler.WebSocketHander;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.util.AttributeKey;


public class WebSocketSession implements Session {

    private Channel channel;

    public WebSocketSession(Channel channel) {
        this.channel = channel;

    }

    @Override
    public void write(Object... messages) {
        if (!channel.isActive()) return;
        for (Object message : messages) {
            if (channel.pipeline().context(WebSocketHander.class.getSimpleName()) != null) {
                channel.pipeline().context(WebSocketHander.class.getSimpleName()).write(message);
            } else {
                channel.write(message);
            }
        }
    }

    @Override
    public void flush() {
        if (!channel.isActive()) return;
        channel.flush();
    }

    @Override
    public void writeAndFlush(Object... messages) {
        write(messages);
        flush();
    }

    @Override
    public void close() {
        channel.writeAndFlush(new CloseWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public boolean isClosed() {
        return !channel.isActive();
    }

    @Override
    public <T> void setAttribute(AttributeKey name, T value) {
        this.channel.attr(name).set(value);
    }

    @Override
    public <T> T getAttribute(AttributeKey<T> name) {
        return this.channel.attr(name).get();
    }

    @Override
    public <T> T removeAttribute(AttributeKey<T> name) {
        return this.channel.attr(name).getAndSet(null);

    }


}
