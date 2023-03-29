package com.ssk.zsaltedfish.netty.webscoket.server;


import com.ssk.zsaltedfish.netty.webscoket.config.WebSocketProperties;
import com.ssk.zsaltedfish.netty.webscoket.server.context.WebSocketServerApplicationContext;
import com.ssk.zsaltedfish.netty.webscoket.server.event.WebSocketServerInitializedEvent;
import com.ssk.zsaltedfish.netty.webscoket.server.handler.DistributeHander;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.GracefulShutdownCallback;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 开启端点服务
 */
@Slf4j
public class WebSocketServer implements WebServer, SmartLifecycle, ApplicationContextAware {

    private final WebSocketProperties socketProperties;


    private final DistributeHander distributeHander;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private ChannelFuture future;
    private EventLoopGroup workerLoopGroup;
    private EventLoopGroup eventLoopGroup;
    private ServerBootstrap serverBootstrap;
    private ApplicationContext context;

    @Autowired
    public WebSocketServer(WebSocketProperties socketProperties, DistributeHander distributeHander) throws InterruptedException {
        this.socketProperties = socketProperties;
        this.distributeHander = distributeHander;
        init();
    }


    public void init() throws InterruptedException {
        workerLoopGroup = new NioEventLoopGroup(socketProperties.getLoop().getWorkerThreadCount());
        eventLoopGroup = new NioEventLoopGroup(socketProperties.getLoop().getEventThreadCount());
        serverBootstrap =
                new ServerBootstrap()
                        .group(workerLoopGroup, eventLoopGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, socketProperties.getOption().getConnectTimeoutMillis())
                        .option(ChannelOption.SO_REUSEADDR, socketProperties.getOption().getSoReuseaddr())
                        .option(ChannelOption.SO_BACKLOG, socketProperties.getOption().getSoBacklog())
                        .childHandler(new HttpInitializerHander())
                        .childOption(ChannelOption.SO_KEEPALIVE, socketProperties.getChildOption().getSoKeepalive())
                        .childOption(ChannelOption.SO_LINGER, socketProperties.getChildOption().getSoLinger())
                        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                                new WriteBufferWaterMark(socketProperties.getChildOption().getWriteBufferLowWaterMark(),
                                        socketProperties.getChildOption().getWriteBufferHighWaterMark()))
                        .childOption(ChannelOption.AUTO_READ, socketProperties.getChildOption().getAutoRead())
                        .childOption(ChannelOption.TCP_NODELAY, socketProperties.getChildOption().getTcpNodelay());
        if (socketProperties.getChildOption().getSoRcvbuf() != -1) {
            serverBootstrap.childOption(ChannelOption.SO_RCVBUF, socketProperties.getChildOption().getSoRcvbuf());
        }
        if (socketProperties.getChildOption().getSoSndbuf() != -1) {
            serverBootstrap.childOption(ChannelOption.SO_SNDBUF, socketProperties.getChildOption().getSoSndbuf());
        }

    }

    private SslContext buildSSLContext() throws IOException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {

        SslContextBuilder ssl = SslContextBuilder.forServer(
                buildKeyManagerFactory()
        ).trustManager(
                buildTrustManagerFactory()
        ).clientAuth(ClientAuth.NONE);
        return ssl.build();
    }

    private KeyManagerFactory buildKeyManagerFactory() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {

        KeyStore keyStore = KeyStore.getInstance(socketProperties.getSsl().getKeyStoreType());
        if (!StringUtils.hasText(socketProperties.getSsl().getKeyStore())) {
            return null;
        }
        ResourceLoader keyStoreResourceLoader = new DefaultResourceLoader();
        Resource keyStoreResource = keyStoreResourceLoader.getResource(socketProperties.getSsl().getKeyStore());
        char[] storePassword = !StringUtils.hasText(socketProperties.getSsl().getKeyStorePassword()) ?
                null : socketProperties.getSsl().getKeyStorePassword().trim().toCharArray();
        if (storePassword == null) {
            return null;
        }
        keyStore.load(keyStoreResource.getInputStream(), storePassword);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        char[] keyPassword = !StringUtils.hasText(socketProperties.getSsl().getKeyPassword()) ?
                null : socketProperties.getSsl().getKeyPassword().trim().toCharArray();
        if (keyPassword == null) {
            keyPassword = storePassword;
        }
        keyManagerFactory.init(keyStore, keyPassword);
        return keyManagerFactory;
    }

    private TrustManagerFactory buildTrustManagerFactory() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        KeyStore trustStore = KeyStore.getInstance(socketProperties.getSsl().getTrustStoreType());
        if (!StringUtils.hasText(socketProperties.getSsl().getTrustStore())) {
            return null;
        }
        ResourceLoader keyStoreResourceLoader = new DefaultResourceLoader();
        Resource trustStoreResource = keyStoreResourceLoader.getResource(socketProperties.getSsl().getTrustStore());
        char[] storePassword = !StringUtils.hasText(socketProperties.getSsl().getTrustStorePassword()) ?
                null : socketProperties.getSsl().getTrustStorePassword().trim().toCharArray();
        if (storePassword == null) {
            return null;
        }
        trustStore.load(trustStoreResource.getInputStream(), storePassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory;
    }

    @SneakyThrows
    @Override
    public void start() {
        if (!isRunning()) {
            //ChannelFuture future = null;
            if ("localhost".equals(socketProperties.getHost())) {
                future = serverBootstrap.bind(socketProperties.getPort()).sync();
            } else {
                try {
                    future = serverBootstrap.bind(new InetSocketAddress(InetAddress.getByName(socketProperties.getHost()), socketProperties.getPort())).sync();
                } catch (UnknownHostException e) {
                    future =
                            serverBootstrap.bind(InetSocketAddress.createUnresolved(socketProperties.getHost()
                                    , socketProperties.getPort())).sync();
                    // e.printStackTrace();
                }

            }
            log.info("{}", socketProperties);
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        eventLoopGroup.shutdownGracefully().awaitUninterruptibly();
                        workerLoopGroup.shutdownGracefully().awaitUninterruptibly();
                    }
            ));
            isRunning.compareAndSet(true, false);
            this.context.publishEvent(new WebSocketServerInitializedEvent(this,
                    new WebSocketServerApplicationContext(this)));
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            eventLoopGroup.shutdownGracefully().awaitUninterruptibly();
            workerLoopGroup.shutdownGracefully().awaitUninterruptibly();
            isRunning.compareAndSet(true, false);
        }
    }

    @Override
    public int getPort() {
        return socketProperties.getPort();
    }

    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        stop();
        WebServer.super.shutDownGracefully(callback);
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public boolean isAutoStartup() {
        return SmartLifecycle.super.isAutoStartup();
    }

    @Override
    public void stop(Runnable callback) {
        SmartLifecycle.super.stop(callback);
    }

    @Override
    public int getPhase() {
        return SmartLifecycle.super.getPhase();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    class HttpInitializerHander extends ChannelInitializer<NioSocketChannel> {

        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            if (socketProperties.getReadIdle() != -1 || socketProperties.getWriteIdle() != -1) {
                int readId = socketProperties.getReadIdle();
                int writeId = socketProperties.getWriteIdle();
                pipeline.addLast(new IdleStateHandler(readId, writeId, 0, TimeUnit.SECONDS));
            }
            if (socketProperties.getSsl().isEnable()) {
                pipeline.addLast(buildSSLContext().newHandler(ch.alloc()));
            }
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));

            CorsConfigBuilder corsConfigBuilder =
                    CorsConfigBuilder.forOrigins(socketProperties.getCorsOrigins()).allowNullOrigin();
            if (socketProperties.getCorsAllowCredentials()) {
                corsConfigBuilder.allowCredentials();
            }

            pipeline.addLast(new CorsHandler(corsConfigBuilder.build()));
            pipeline.addLast(DistributeHander.class.getSimpleName(), distributeHander);
        }
    }
}
