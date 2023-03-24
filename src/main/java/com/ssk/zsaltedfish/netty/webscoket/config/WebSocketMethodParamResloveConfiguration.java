package com.ssk.zsaltedfish.netty.webscoket.config;


import com.ssk.zsaltedfish.netty.webscoket.support.json.JsonParser;
import com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove.*;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.ValidatorFactory;

@Configuration
public class WebSocketMethodParamResloveConfiguration {

    @Bean
    public WebSocketMethodParamReslove createSessions() {
        return new SessionWebSocketMethodParamReslove();
    }

    @Bean
    public WebSocketMethodParamReslove createText() {
        return new TextWebSocketMethodParamReslove();
    }

    @Bean
    public WebSocketMethodParamReslove createException() {
        return new ExceptionSocketMethodParamReslove();
    }

    @Bean
    public WebSocketMethodParamReslove createHttpHeaders() {
        return new HttpHeadersWebSocketMethodParamReslove();
    }


    @Bean
    public WebSocketMethodParamReslove createRequestParam(AbstractBeanFactory factory) {
        return new RequestParamWebSocketMethodParamReslove(factory.getTypeConverter());
    }

    @Bean
    public WebSocketMethodParamReslove createRequestBody(JsonParser jsonParser, ValidatorFactory validatorFactory) {
        return new RequestBodyWebSocketMethodParamReslove(jsonParser, validatorFactory);
    }

    @Bean
    public WebSocketMethodParamReslove createPath(AbstractBeanFactory factory) {
        return new PathWebSocketMethodParamReslove(factory.getTypeConverter());
    }

}
