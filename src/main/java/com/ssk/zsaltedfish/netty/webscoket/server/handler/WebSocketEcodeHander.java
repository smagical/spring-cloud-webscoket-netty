package com.ssk.zsaltedfish.netty.webscoket.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * 编码器
 */
@ChannelHandler.Sharable
public abstract class WebSocketEcodeHander extends MessageToMessageEncoder {
}
