package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;

import static com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode.METHOD_PARAMETER_RESOVE_ERROR;

/**
 * 处理{@link  Exception}类型参数
 */
public class ExceptionSocketMethodParamReslove extends AbstractWebSocketMethodParamReslove<Exception> {

    @Override
    public boolean support(MethodParameter parameter) {
        return Exception.class.isAssignableFrom(parameter.getParameterType());
    }

    @SneakyThrows
    @Override
    public Exception resolve(Channel channel, MethodParameter parameter, Object object) {
        if (object == null) return null;
        if (Exception.class.isAssignableFrom(object.getClass())) {
            return ((Exception) object);
        }
        throw new WebScoketExcpetion(METHOD_PARAMETER_RESOVE_ERROR, object.getClass() + "处理对象不是异常信息");
    }
}
