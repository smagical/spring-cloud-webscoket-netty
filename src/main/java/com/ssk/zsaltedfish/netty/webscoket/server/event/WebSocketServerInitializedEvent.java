package com.ssk.zsaltedfish.netty.webscoket.server.event;

import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;

public class WebSocketServerInitializedEvent extends WebServerInitializedEvent {
    private final WebServerApplicationContext context;
    private WebServerApplicationContext webServerApplicationContext;

    public WebSocketServerInitializedEvent(WebServer webServer, WebServerApplicationContext context) {
        super(webServer);
        this.context = context;
    }

    @Override
    public WebServerApplicationContext getApplicationContext() {
        return context;
    }
}
