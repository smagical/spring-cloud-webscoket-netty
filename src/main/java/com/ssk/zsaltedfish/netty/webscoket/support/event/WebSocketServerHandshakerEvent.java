package com.ssk.zsaltedfish.netty.webscoket.support.event;

public class WebSocketServerHandshakerEvent implements Event {

    private Object msg;

    public WebSocketServerHandshakerEvent() {
    }

    public WebSocketServerHandshakerEvent(Object msg) {
        this.msg = msg;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }
}
