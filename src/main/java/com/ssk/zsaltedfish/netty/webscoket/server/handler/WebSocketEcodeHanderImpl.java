package com.ssk.zsaltedfish.netty.webscoket.server.handler;

import com.ssk.zsaltedfish.netty.webscoket.support.json.JsonParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 简单编码器,将{@link  com.ssk.zsaltedfish.netty.webscoket.pojo.Session}
 * 写入的数据转换为 {@link  io.netty.handler.codec.http.websocketx.WebSocketFrame}
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketEcodeHanderImpl extends WebSocketEcodeHander {

    private final JsonParser jsonParser;

    public WebSocketEcodeHanderImpl(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        if (msg == null) return;

        //todo 添加消息转换器
        if (HttpResponse.class.isAssignableFrom(msg.getClass())) {
            ReferenceCountUtil.retain(msg);
            out.add(msg);
        } else if (msg.getClass().isArray() && byte[].class.isAssignableFrom(msg.getClass())) {
            out.add(new BinaryWebSocketFrame(Unpooled.copiedBuffer((byte[]) msg)));
        } else if (msg.getClass().isArray() && Byte[].class.isAssignableFrom(msg.getClass())) {
            Byte[] temp = (Byte[]) msg;
            byte[] b = new byte[temp.length];
            for (int i = 0; i < temp.length; i++) {
                b[i] = temp[i].byteValue();
            }
            out.add(new BinaryWebSocketFrame(Unpooled.copiedBuffer(b)));
        } else {
            out.add(new TextWebSocketFrame(jsonParser.toJSON(msg)));
        }
    }

}
