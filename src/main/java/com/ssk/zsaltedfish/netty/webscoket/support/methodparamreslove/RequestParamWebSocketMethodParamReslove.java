package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.annotation.param.RequestParam;
import com.ssk.zsaltedfish.netty.webscoket.pojo.Session;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import org.springframework.beans.TypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class RequestParamWebSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<Object> {

    public final static AttributeKey<Map<String, List<String>>> WEB_SOCKET_PARAM_KEY =
            AttributeKey.<Map<String, List<String>>>newInstance("WEB_SOCKET_PARAM_KEY");
    private TypeConverter typeConverter;

    public RequestParamWebSocketMethodParamReslove(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public boolean support(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    @SneakyThrows
    @Override
    public Object resolve(Channel channel, MethodParameter parameter, Object object) {
        Map<String, List<String>> parameters =
                channel.attr(WEB_SOCKET_PARAM_KEY).get();

        if (parameters == null) {
            String uri = channel.attr(Session.WEB_SOCKET_URI_KEY).get();
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            parameters = queryStringDecoder.parameters();
            channel.attr(WEB_SOCKET_PARAM_KEY).set(parameters);
        }

        String name = parameter.getParameterAnnotation(RequestParam.class).name();
        List<String> param = parameters.get(name);
        if (!StringUtils.hasText(name)) {
            name = parameter.getParameterName();
        }
        if (param == null) return null;
        if (List.class.isAssignableFrom(parameter.getParameterType())) {
            return typeConverter.convertIfNecessary(param, parameter.getParameterType(), parameter);
        } else {
            return typeConverter.convertIfNecessary(param.get(0), parameter.getParameterType(), parameter);
        }
    }

}
