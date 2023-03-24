package com.ssk.zsaltedfish.netty.webscoket.annotation;

import com.ssk.zsaltedfish.netty.webscoket.config.WebSocketScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(WebSocketScan.class)
public @interface EnableWebScoket {

}
