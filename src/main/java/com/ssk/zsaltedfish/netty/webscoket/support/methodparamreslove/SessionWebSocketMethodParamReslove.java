package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.pojo.Session;
import com.ssk.zsaltedfish.netty.webscoket.pojo.WebSocketSession;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.springframework.core.MethodParameter;

public class SessionWebSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<Session> {
    public AttributeKey<WebSocketSession> WEB_SOCKET_SESSION_KEY =
            AttributeKey.<WebSocketSession>newInstance("WEB_SOCKET_SESSION_KEY");

    @Override
    public boolean support(MethodParameter parameter) {
        return Session.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Session resolve(Channel channel, MethodParameter parameter, Object object) {
        if (!channel.hasAttr(WEB_SOCKET_SESSION_KEY)) {
            channel.attr(WEB_SOCKET_SESSION_KEY).set(new WebSocketSession(channel));
        }
        return channel.attr(WEB_SOCKET_SESSION_KEY).get();
    }
}
