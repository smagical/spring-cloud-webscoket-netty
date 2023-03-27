package com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove;

/**
 * 处理websocket方法参数
 * 添加order方法，参数处理器顺序，当处理参数时，会从所有参数参数处理器找到第一个处理
 * 重写 {@link AbstractWebSocketMethodParamReslove#equals} 和 {@link AbstractWebSocketMethodParamReslove#hashCode()}
 *
 * @param <T>
 */

public abstract class AbstractWebSocketMethodParamReslove<T> implements WebSocketMethodParamReslove<T> {

    private Integer order = Integer.MAX_VALUE / 2;

    protected String getResolvedName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        if (!this.getClass().isAssignableFrom(obj.getClass())) return false;
        return getResolvedName().equals(((AbstractWebSocketMethodParamReslove) obj).getResolvedName());
    }

    @Override
    public String toString() {
        return getResolvedName() + " " + super.toString();
    }

    @Override
    public int hashCode() {
        return getResolvedName().hashCode();
    }

    @Override
    public Integer getOrder() {
        return this.order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
