package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;

import static com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode.METHOD_PARAMETER_RESOVE_ERROR;

public class TextWebSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<String> {
    public TextWebSocketMethodParamReslove() {
        setOrder(Integer.MAX_VALUE);
    }

    @Override
    public boolean support(MethodParameter parameter) {
        return String.class.isAssignableFrom(parameter.getParameterType());
    }

    @SneakyThrows
    @Override
    public String resolve(Channel channel, MethodParameter parameter, Object object) {
        if (object == null) return null;
        if (WebSocketFrame.class.isAssignableFrom(object.getClass())) {
            if (TextWebSocketFrame.class.isAssignableFrom(object.getClass())) {
                return ((TextWebSocketFrame) object).text();
            }
            throw new WebScoketExcpetion(METHOD_PARAMETER_RESOVE_ERROR, object.getClass() + "处理对象不是字符串窗口");
        }
        return object.toString();
    }
}
