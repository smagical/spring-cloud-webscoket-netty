package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.annotation.param.PathVariable;
import com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.pojo.WebSocketSession;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.WebSocketHander;
import com.ssk.zsaltedfish.netty.webscoket.support.PathServerEndpointMapping;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import org.springframework.beans.TypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.util.AntPathMatcher;

import java.util.Map;

/**
 * 处理带有{@link  PathVariable}类型参数
 */
public class PathWebSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<Object> {

    public final static AttributeKey<Map<String, String>> WEB_SOCKET_PATH_PARAM_KEY =
            AttributeKey.newInstance("WEB_SOCKET_PATH_PARAM_KEY");
    private AntPathMatcher antPathMatcher;
    private final TypeConverter typeConverter;

    public PathWebSocketMethodParamReslove(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public boolean support(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathVariable.class);
    }

    @SneakyThrows
    @Override
    public Object resolve(Channel channel, MethodParameter parameter, Object object) {
        Map<String, String> parameters = channel.attr(WEB_SOCKET_PATH_PARAM_KEY).get();
        if (parameters == null) {
            String uri = channel.attr(WebSocketSession.WEB_SOCKET_URI_KEY).get();
            if (uri == null) {
                throw new WebScoketExcpetion(ExceptionCode.NOT_FOUND_URI_ERROR, "" +
                        "找不到URI信息");
            }
            PathServerEndpointMapping.ServerEndpointMethodMappingAndPath mappingAndPath =
                    channel.attr(WebSocketHander.SERVER_ENDPOINT_METHOD_MAPPING_KEY).get();
            if (mappingAndPath == null) {
                throw new WebScoketExcpetion(ExceptionCode.NOT_FOUND_SERVERENDPOINT_ERROR, "" +
                        "找不到端点信息");
            }
            int index = uri.indexOf("?");
            if (index != -1) {
                uri = uri.substring(0, index);
            }
            parameters = antPathMatcher.extractUriTemplateVariables(mappingAndPath.getPath(), uri);
            channel.attr(WEB_SOCKET_PATH_PARAM_KEY).set(parameters);
        }
        String param = parameters.get(parameter.getParameterName());
        if (param == null) return null;
        return typeConverter.convertIfNecessary(param, parameter.getParameterType(), parameter);

    }

}
