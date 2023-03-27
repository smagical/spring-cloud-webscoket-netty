package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.pojo.WebSocketSession;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.core.MethodParameter;

/**
 * 处理{@link  HttpHeaders}类型参数
 */
public class HttpHeadersWebSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<HttpHeaders> {
    @Override
    public boolean support(MethodParameter parameter) {
        return HttpHeaders.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public HttpHeaders resolve(Channel channel, MethodParameter parameter, Object object) {
        if (object != null && HttpHeaders.class.isAssignableFrom(object.getClass())) {
            return (HttpHeaders) object;
        }
        if (object != null && FullHttpRequest.class.isAssignableFrom(object.getClass())) {
            return ((FullHttpRequest) object).headers();
        }
        return channel.attr(WebSocketSession.WEB_SOCKET_HEADERS_KEY).get();

    }

}
