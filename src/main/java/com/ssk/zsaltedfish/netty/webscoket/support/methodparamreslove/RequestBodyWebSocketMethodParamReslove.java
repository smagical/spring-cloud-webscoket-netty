package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.annotation.invoke.OnMessage;
import com.ssk.zsaltedfish.netty.webscoket.annotation.param.RequestBody;
import com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.support.json.JsonParser;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
/**
 * 处理带有{@link  RequestBody}类型参数，支持 json转bean
 */
public class RequestBodyWebSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<Object> {

    private final JsonParser jsonParser;
    private final ValidatorFactory validatorFactory;

    public RequestBodyWebSocketMethodParamReslove(JsonParser jsonParser, ValidatorFactory validatorFactory) {
        this.jsonParser = jsonParser;
        this.validatorFactory = validatorFactory;
    }

    @Override
    public boolean support(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class) &&
                parameter.getMethod().getDeclaredAnnotationsByType(OnMessage.class) != null;
    }

    @SneakyThrows
    @Override
    public Object resolve(Channel channel, MethodParameter parameter, Object object) {
        Object t = doSolve(channel, parameter, object);
        if (t != null) {
            for (ConstraintViolation<Object> violation : validatorFactory.getValidator().validate(t)) {
                throw new RuntimeException(violation.getMessage());
            }
        }
        return t;
    }

    private Object doSolve(Channel channel, MethodParameter parameter, Object object) throws WebScoketExcpetion {
        if (String.class.isAssignableFrom(parameter.getParameterType()) || Object.class.equals(object.getClass())) {
            return sovleString(object);
        } else if (parameter.getParameterType().isArray() &&
                byte[].class.isAssignableFrom(parameter.getParameterType())) {
            return sovleByte(parameter, object);

        } else if (parameter.getParameterType().isArray() &&
                Byte[].class.isAssignableFrom(parameter.getParameterType())) {
            byte[] temp = sovleByte(parameter, object);
            Byte[] b = new Byte[temp.length];
            for (int i = 0; i < temp.length; i++) {
                b[i] = Byte.valueOf(temp[i]);
            }
            return b;

        } else {
            String str = sovleString(object);
            if (str == null) return null;
            if (jsonParser.validate(str)) {
                return jsonParser.getObject(str, parameter.getParameterType());
            } else {
                throw new WebScoketExcpetion(ExceptionCode.METHOD_PARAMETER_RESOVE_ERROR, "" +
                        object.getClass().getName() +
                        " " + object + "无法处理转换" + parameter.getParameterType().getName());
            }

        }
    }

    private String sovleString(Object object) {
        if (object == null) return null;
        if (WebSocketFrame.class.isAssignableFrom(object.getClass())) {
            if (TextWebSocketFrame.class.isAssignableFrom(object.getClass())) {
                return ((TextWebSocketFrame) object).text();
            }
        }
        return object.toString();
    }

    private byte[] sovleByte(MethodParameter parameter, Object object) throws WebScoketExcpetion {
        if (BinaryWebSocketFrame.class.isAssignableFrom(object.getClass())) {
            return ((BinaryWebSocketFrame) object).content().array();
        } else if (TextWebSocketFrame.class.isAssignableFrom(object.getClass())) {
            return ((TextWebSocketFrame) object).text().getBytes();
        } else if (byte[].class.isAssignableFrom(object.getClass())) {
            return (byte[]) object;
        } else {
            throw new WebScoketExcpetion(ExceptionCode.METHOD_PARAMETER_RESOVE_ERROR, "" +
                    object.getClass().getName() +
                    " " + object + "无法处理转换" + parameter.getParameterType().getName());
        }
    }


}
