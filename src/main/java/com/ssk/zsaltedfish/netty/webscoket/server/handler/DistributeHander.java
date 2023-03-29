package com.ssk.zsaltedfish.netty.webscoket.server.handler;

import com.ssk.zsaltedfish.netty.webscoket.config.WebSocketProperties;
import com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.pojo.WebSocketSession;
import com.ssk.zsaltedfish.netty.webscoket.support.PathServerEndpointMapping;
import com.ssk.zsaltedfish.netty.webscoket.support.event.WebSocketServerHandshakerEvent;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * http升级websocket协议，并根据请求连接给通道绑定相应端点
 */
@Slf4j
@ChannelHandler.Sharable
public class DistributeHander extends ChannelInboundHandlerAdapter {

    private final PathServerEndpointMapping pathServerEndpointMapping;
    private final WebSocketHander webSocketHander;
    private final WebSocketEcodeHander webSocketEcodeHander;
    private final WebSocketProperties properties;
    private final NioEventLoopGroup nioEventLoopGroup;

    public DistributeHander(PathServerEndpointMapping pathServerEndpointMapping,
                            WebSocketHander webSocketHander, WebSocketEcodeHander webSocketEcodeHander,
                            WebSocketProperties socketProperties,
                            NioEventLoopGroup nioEventLoopGroup) {
        this.pathServerEndpointMapping = pathServerEndpointMapping;
        this.webSocketHander = webSocketHander;
        this.webSocketEcodeHander = webSocketEcodeHander;
        this.properties = socketProperties;
        this.nioEventLoopGroup = nioEventLoopGroup;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        try {
            if (msg instanceof FullHttpRequest) {
                HttpHeaders headers = ((FullHttpRequest) msg).headers().copy();
                if (isWebSocketRequest(headers)) {

                    WebSocketServerHandshakerFactory handshakerFactory =
                            new WebSocketServerHandshakerFactory(getlocalhost(((FullHttpRequest) msg).copy()), properties.getSubProtocol(), true);
                    WebSocketServerHandshaker serverHandshaker = handshakerFactory.newHandshaker((FullHttpRequest) msg);
                    if (headers == null) {
                        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel()).addListener(
                                ChannelFutureListener.CLOSE
                        );
                    } else {
                        PathServerEndpointMapping.ServerEndpointMethodMappingAndPath serverEndpointMethodMapping = null;
                        try {
                            serverEndpointMethodMapping = pathServerEndpointMapping.getServerEndpointMappingAndPath(((FullHttpRequest) msg).uri());
                        } catch (WebScoketExcpetion e) {
                            if (ExceptionCode.NOT_FOUND_SERVERENDPOINT_ERROR.equals(e.getCode())) {
                                ctx.writeAndFlush(
                                                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND))
                                        .addListener(ChannelFutureListener.CLOSE);
                            } else {
                                ctx.writeAndFlush(
                                                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR))
                                        .addListener(ChannelFutureListener.CLOSE);
                            }
                        }
                        ctx.channel().attr(WebSocketHander.SERVER_ENDPOINT_METHOD_MAPPING_KEY)
                                .set(serverEndpointMethodMapping);
                        ctx.channel().attr(WebSocketSession.WEB_SOCKET_HEADERS_KEY).set(((FullHttpRequest) msg).headers());
                        ctx.channel().attr(WebSocketSession.WEB_SOCKET_URI_KEY).set(((FullHttpRequest) msg).uri());
                        if (log.isDebugEnabled()) {
                            log.debug("remote ip [{}]", ctx.channel().remoteAddress());
                            log.debug("path [{}]", ((FullHttpRequest) msg).uri());
                            log.debug("headers [{}]", ((FullHttpRequest) msg).headers().copy());
                        }
                        webSocketHander.doBeforeHandshake(ctx.channel());
                        if (ctx.channel().closeFuture().isDone()) return;
                        serverHandshaker.handshake(ctx.channel(), (FullHttpRequest) msg).addListener(
                                future -> {
                                    if (future.isSuccess()) {
                                        ctx.channel().attr(WebSocketHander.WEBSOCKET_HANDSHAKE_KEY).set(serverHandshaker);
                                        ctx.pipeline().addBefore(nioEventLoopGroup,
                                                DistributeHander.class.getSimpleName(),
                                                WebSocketEcodeHander.class.getSimpleName(),
                                                webSocketEcodeHander);
                                        ctx.pipeline().addLast(WebSocketHander.class.getSimpleName(), webSocketHander);
                                        ctx.fireUserEventTriggered(new WebSocketServerHandshakerEvent(msg));

                                    } else {
                                        ctx.channel().close();
                                    }
                                }
                        );

                    }
                } else {
                    if (log.isDebugEnabled())
                        log.debug(((FullHttpRequest) msg).uri());
                    ctx.writeAndFlush(new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, HttpResponseStatus.OK, DefaultFullHttpResponse.EMPTY_LAST_CONTENT.content()
                    )).addListener(future -> {
                        ctx.channel().close();
                    });

                }

            } else {
                ctx.fireExceptionCaught(
                        new WebScoketExcpetion(ExceptionCode.AGREEMENT_NOT_IS_HTTP_ERROR, "请求不是HTTP协议")
                );
            }
        } finally {
            ctx.pipeline().remove(this);
        }
    }

    /**
     * 简单判断是否为websocket请求
     *
     * @param headers
     *
     * @return
     */
    private boolean isWebSocketRequest(HttpHeaders headers) {
        if (!headers.contains(HttpHeaderNames.CONNECTION) ||
                !"Upgrade".equalsIgnoreCase(headers.getAsString(HttpHeaderNames.CONNECTION).trim()))
            return false;
        if (!headers.contains(HttpHeaderNames.UPGRADE) || !"websocket".equalsIgnoreCase(headers.getAsString(HttpHeaderNames.UPGRADE).trim()))
            return false;
        return headers.contains(HttpHeaderNames.SEC_WEBSOCKET_KEY) && headers.contains(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
    }

    private String getlocalhost(FullHttpRequest request) {
        String location = request.headers().get(HttpHeaderNames.HOST) + request.uri();
        if (this.properties.getSsl().isEnable()) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

    public PathServerEndpointMapping getPathServerEndpointMapping() {
        return pathServerEndpointMapping;
    }
}
