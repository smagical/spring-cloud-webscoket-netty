package com.ssk.zsaltedfish.netty.webscoket;


import com.ssk.zsaltedfish.netty.webscoket.annotation.EnableWebScoket;
import com.ssk.zsaltedfish.netty.webscoket.config.WebSocketProperties;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.DistributeHander;
import com.ssk.zsaltedfish.netty.webscoket.support.PathServerEndpointMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableWebScoket
public class SpringCloudWebsocketNettyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context
                = SpringApplication.run(SpringCloudWebsocketNettyApplication.class, args);
        System.out.println(context.getBean(WebSocketProperties.class));
        System.out.println(context.getBean(PathServerEndpointMapping.class).equals(
                context.getBean(DistributeHander.class).getPathServerEndpointMapping()
        ));

    }

}
