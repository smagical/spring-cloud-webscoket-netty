package com.ssk.zsaltedfish.netty.webscoket.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = WebSocketProperties.PREFIX)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketProperties {
    public static final String PREFIX = "netty.websocket";
    private String host = "localhost";
    private Integer port = 8080;
    private String contextPath = "/";
    private String subProtocol = null;
    private Ssl ssl = new Ssl();
    private String[] corsOrigins = new String[0];
    private Boolean corsAllowCredentials = false;
    private Integer readIdle = 0;
    private Integer writeIdle = 0;

    //netty配置
    private Option option = new Option();
    private ChildOption childOption = new ChildOption();
    private Loop loop = new Loop();


    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ssl {

        private boolean enable = false;

        private String keyPassword;

        private String keyStore;            //e.g. classpath:server.jks

        private String keyStorePassword;

        private String keyStoreType = "JKS";        //e.g. JKS

        private String trustStore;

        private String trustStorePassword;

        private String trustStoreType = "JKS";


    }


    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {
        /**
         * <link href="https://blog.csdn.net/zhongzunfa/article/details/94590670"/>
         */
        //请求窗口
        private Integer soBacklog = 128;

        //重复使用端口
        private Boolean soReuseaddr = false;

        //超时
        private Integer connectTimeoutMillis = 30000;

    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildOption {

        //如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文。
        private Boolean soKeepalive = false;

        //关闭延迟时间
        private Integer soLinger = -1;

        //高位缓冲 超过后Channel.isWritable()返回false
        private Integer writeBufferHighWaterMark = 65536;

        //地位缓冲 低过后Channel.isWritable()返回true
        private Integer writeBufferLowWaterMark = 32768;

        //true 自动调用channel.read()
        private Boolean autoRead = true;

        //是否启用Nagle算法
        private Boolean tcpNodelay = false;

        private Integer soRcvbuf = -1;

        private Integer soSndbuf = -1;


    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Loop {

        private Integer workerThreadCount = 1;

        private Integer eventThreadCount = 64;

        private Integer socketThreadCount = 64;


    }
}
