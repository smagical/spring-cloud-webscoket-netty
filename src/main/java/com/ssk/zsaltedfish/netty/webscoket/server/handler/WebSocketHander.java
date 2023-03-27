package com.ssk.zsaltedfish.netty.webscoket.server.handler;

import com.ssk.zsaltedfish.netty.webscoket.annotation.invoke.*;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.pojo.AnnatationMethodParam;
import com.ssk.zsaltedfish.netty.webscoket.support.PathServerEndpointMapping;
import com.ssk.zsaltedfish.netty.webscoket.support.ServerEndpointMethodMapping;
import com.ssk.zsaltedfish.netty.webscoket.support.event.WebSocketServerHandshakerEvent;
import com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove.WebSocketMethodParamReslove;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;

import java.util.Map;

import static com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode.NOT_FOUND_SERVERENDPOINT_ERROR;

/**
 * webscoket处理器，负责调用相关端点的方法
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketHander extends ChannelInboundHandlerAdapter {
    public static final AttributeKey<WebSocketServerHandshaker> WEBSOCKET_HANDSHAKE_KEY = AttributeKey.newInstance("WEBSOCKET_HANDSHAKE_KEY");
    public static final AttributeKey<PathServerEndpointMapping.ServerEndpointMethodMappingAndPath> SERVER_ENDPOINT_METHOD_MAPPING_KEY
            = AttributeKey.newInstance("SERVER_ENDPOINT_METHOD_MAPPING_KEY");

    /**
     * 处理连接打开事件
     *
     * @param ctx
     * @param evt
     *
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerHandshakerEvent) {
            if (log.isDebugEnabled()) {
                log.debug("WebSocketHander WebSocketServerHandshakerEvent");
            }
            Object msg = ((WebSocketServerHandshakerEvent) evt).getMsg();
            doOnOpen(ctx.channel(), msg);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        try {
            if (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame) {
                doOnMessage(ctx.channel(), msg);
            } else if (msg instanceof PingWebSocketFrame) {
                PongWebSocketFrame socketMsg = (PongWebSocketFrame) msg;
                ctx.writeAndFlush(new PongWebSocketFrame(socketMsg.content()));
            } else if (msg instanceof PongWebSocketFrame) {
                PingWebSocketFrame socketMsg = (PingWebSocketFrame) msg;
                ctx.writeAndFlush(new PingWebSocketFrame(socketMsg.content()));
            } else if (msg instanceof CloseWebSocketFrame) {
                CloseWebSocketFrame socketMsg = (CloseWebSocketFrame) msg;
                ReferenceCountUtil.retain(msg);
                if (!ctx.channel().hasAttr(WEBSOCKET_HANDSHAKE_KEY)) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
                WebSocketServerHandshaker serverHandshaker =
                        ctx.channel().attr(WEBSOCKET_HANDSHAKE_KEY).get();
                serverHandshaker.close(ctx, socketMsg).addListener(
                        future -> doOnClose(ctx.channel(), null)
                );
                if (log.isDebugEnabled()) {
                    log.debug("WEBSOCKET_HANDSHAKE [{}] CLOSE", ctx.channel().remoteAddress());
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 反射调用方法
     *
     * @param channel
     * @param msg
     * @param annotation
     *
     * @return
     */
    public Object invoke(Channel channel, Object msg, Class<?> annotation) {
        try {

            ServerEndpointMethodMapping serverEndpointMethodMapping = getChannelMethodMapping(channel);
            if (serverEndpointMethodMapping == null)
                throw new WebScoketExcpetion(NOT_FOUND_SERVERENDPOINT_ERROR

                        , "没有把端点注册到通道");
            if (log.isDebugEnabled()) {
                log.debug("调用{}方法  参数{}", annotation.getSimpleName(), msg);
            }
            AnnatationMethodParam methodParameterAndReslove =
                    serverEndpointMethodMapping.getMethodParameterAndReslove(annotation);
            if (methodParameterAndReslove == null) return null;
            Object o = serverEndpointMethodMapping.getBean();
            Object[] param = new Object[methodParameterAndReslove.getMethodParameterAndReslove().size()];
            for (Map.Entry<MethodParameter, WebSocketMethodParamReslove> entry : methodParameterAndReslove.getMethodParameterAndReslove().entrySet()) {
                param[entry.getKey().getParameterIndex()] = entry.getValue().resolve(channel, entry.getKey(), msg);
            }
            ReflectionUtils.makeAccessible(methodParameterAndReslove.getMethod());
            return ReflectionUtils.invokeMethod(methodParameterAndReslove.getMethod(), o, param);

        } catch (Exception e) {
            if (OnError.class.isAssignableFrom(annotation)) return null;
            e.printStackTrace();
            doOnError(channel, e);
        }
        return null;
    }

    public void doBeforeHandshake(Channel channel) {
        sendReturn(channel, invoke(channel, null, BeforeHandShake.class));
    }

    public void doOnOpen(Channel channel, Object msg) {
        sendReturn(channel, invoke(channel, msg, OnOpen.class));
    }

    public void doOnClose(Channel channel, Object msg) {
        invoke(channel, msg, OnClose.class);

    }

    public void doOnMessage(Channel channel, Object msg) {
        sendReturn(channel, invoke(channel, msg, OnMessage.class));
    }

    public void doOnError(Channel channel, Object msg) {
        invoke(channel, msg, OnError.class);
    }

    /**
     * 获取通道对应的端点
     *
     * @param channel
     *
     * @return
     */
    private ServerEndpointMethodMapping getChannelMethodMapping(Channel channel) {
        if (channel.hasAttr(SERVER_ENDPOINT_METHOD_MAPPING_KEY))
            return channel.attr(SERVER_ENDPOINT_METHOD_MAPPING_KEY).get().getMapping();
        return null;
    }

    /**
     * 简单处理返回值
     *
     * @param channel
     *
     * @return
     */
    private void sendReturn(Channel channel, Object msg) {
        if (msg == null) return;
        channel.pipeline().context(getClass().getSimpleName()).writeAndFlush(msg);
    }
}
