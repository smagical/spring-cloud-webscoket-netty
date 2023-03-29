package com.ssk.zsaltedfish.netty.webscoket.config;

import com.ssk.zsaltedfish.netty.webscoket.annotation.EnableWebScoket;
import com.ssk.zsaltedfish.netty.webscoket.server.WebSocketServer;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.DistributeHander;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.WebSocketEcodeHander;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.WebSocketEcodeHanderImpl;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.WebSocketHander;
import com.ssk.zsaltedfish.netty.webscoket.support.PathServerEndpointMapping;
import com.ssk.zsaltedfish.netty.webscoket.support.json.FastJsonParser;
import com.ssk.zsaltedfish.netty.webscoket.support.json.GsonJsonParser;
import com.ssk.zsaltedfish.netty.webscoket.support.json.JackJsonParser;
import com.ssk.zsaltedfish.netty.webscoket.support.json.JsonParser;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

@EnableWebScoket
@Configuration
@Import({WebSocketProperties.class})
public class WebSocketAutoConfiguration {

    @Bean
    public WebSocketServer createWebScoketServer(WebSocketProperties socketProperties, JsonParser jsonParser) throws InterruptedException {
        return new WebSocketServer(socketProperties, createDistributeHander(socketProperties, jsonParser));
    }


    @ConditionalOnMissingBean(PathServerEndpointMapping.class)
    @Bean
    public PathServerEndpointMapping createPathServerEndpointMapping() {
        return new PathServerEndpointMapping();
    }

    @ConditionalOnMissingBean(WebSocketHander.class)
    @Bean
    public WebSocketHander createWebSocketHander() {
        return new WebSocketHander();
    }

    @ConditionalOnMissingBean(DistributeHander.class)
    @Bean
    public DistributeHander createDistributeHander(WebSocketProperties socketProperties, JsonParser jsonParser) {
        return new DistributeHander(createPathServerEndpointMapping(), createWebSocketHander(),
                createDecodeHander(jsonParser), socketProperties, new NioEventLoopGroup(socketProperties.getLoop().getSocketThreadCount()));
    }

    @ConditionalOnMissingBean(WebSocketEcodeHander.class)
    @Bean
    public WebSocketEcodeHander createDecodeHander(JsonParser jsonParser) {
        return new WebSocketEcodeHanderImpl(jsonParser);
    }


    // 创建jsonParser

    @ConditionalOnMissingBean(JsonParser.class)
    @Bean
    public JsonParser createJsonParser(AbstractBeanFactory beanFactory) {
        if (ClassUtils.isPresent(
                com.alibaba.fastjson.JSON.class.getName(),
                beanFactory.getBeanClassLoader())) {
            return new FastJsonParser();
        } else if (ClassUtils.isPresent(
                com.fasterxml.jackson.databind.ObjectMapper.class.getName(),
                beanFactory.getBeanClassLoader())) {
            return new JackJsonParser();
        } else if (ClassUtils.isPresent(
                com.google.gson.Gson.class.getName(),
                beanFactory.getBeanClassLoader())) {
            return new GsonJsonParser();
        } else {
            throw new RuntimeException("无法加载json处理器,至少需要一种{fastjson，jackson，Gson }json处理器");
        }
    }

    @ConditionalOnMissingBean(ValidatorFactory.class)
    @Bean
    public ValidatorFactory createValidatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }
}
