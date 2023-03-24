package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

/**
 * 处理websocket方法参数
 *
 * @param <T>
 */
public interface WebSocketMethodParamReslove<T> extends Comparable<WebSocketMethodParamReslove> {

    /**
     * 是否支持
     *
     * @param parameter
     *
     * @return
     */
    public boolean support(MethodParameter parameter);

    /**
     * 处理结果
     *
     * @param channel
     * @param parameter
     * @param object
     *
     * @return
     */
    public T resolve(Channel channel, MethodParameter parameter, Object object);

    public Integer getOrder();

    @Override
    default int compareTo(WebSocketMethodParamReslove o) {
        return getOrder().compareTo(o.getOrder());
    }
}

