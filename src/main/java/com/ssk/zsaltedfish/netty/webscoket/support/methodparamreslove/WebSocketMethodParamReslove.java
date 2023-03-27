package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

public interface WebSocketMethodParamReslove<T> extends Comparable<WebSocketMethodParamReslove> {

    /**
     * 是否支持
     *
     * @param parameter
     *
     * @return
     */
    boolean support(MethodParameter parameter);

    /**
     * 处理结果
     *
     * @param channel
     * @param parameter
     * @param object
     *
     * @return
     */
    T resolve(Channel channel, MethodParameter parameter, Object object);

    Integer getOrder();

    @Override
    default int compareTo(WebSocketMethodParamReslove o) {
        return getOrder().compareTo(o.getOrder());
    }
}

