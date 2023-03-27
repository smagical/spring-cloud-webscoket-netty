package com.ssk.zsaltedfish.netty.webscoket.config;

import com.ssk.zsaltedfish.netty.webscoket.annotation.ServerEndpoint;
import com.ssk.zsaltedfish.netty.webscoket.annotation.WebSocketEndpointScan;
import com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.support.PathServerEndpointMapping;
import com.ssk.zsaltedfish.netty.webscoket.support.ServerEndpointMethodMapping;
import com.ssk.zsaltedfish.netty.webscoket.support.WebSocketMethodParamResloveCollection;
import com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove.WebSocketMethodParamReslove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public class WebSocketScan implements SmartInitializingSingleton, BeanClassLoaderAware, ApplicationContextAware, BeanFactoryAware, ResourceLoaderAware {

    private ApplicationContext context;
    private ResourceLoader resourceLoader;
    private BeanFactory beanFactory;
    private ClassLoader classLoader;
    private PathServerEndpointMapping pathServerEndpointMapping;


    @Autowired
    public void setPathServerEndpointMapping(PathServerEndpointMapping pathServerEndpointMapping) {
        this.pathServerEndpointMapping = pathServerEndpointMapping;

    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            scannerPackages();
            solveWebSocketMeathodParameterResolver();
            scannerWebSocketEndpointBeans();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);

        }
    }

    /**
     * 获取所有参数处理器
     */
    private void solveWebSocketMeathodParameterResolver() {
        String[] webSocketMethodParamResloveNames = context.getBeanNamesForType(WebSocketMethodParamReslove.class);
        if (log.isDebugEnabled())
            log.debug("webSocketMethodParamReslove {}", Arrays.toString(webSocketMethodParamResloveNames));
        for (String webSocketMethodParamResloveName : webSocketMethodParamResloveNames) {
            Object bean = context.getBean(webSocketMethodParamResloveName);
            WebSocketMethodParamResloveCollection.addMethodParameterAndReslove(((WebSocketMethodParamReslove) bean));
        }
    }

    /**
     * 扫描所有端点
     *
     * @throws WebScoketExcpetion
     */
    private void scannerWebSocketEndpointBeans() throws WebScoketExcpetion {

        String[] serverEndpointClassNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
        if (log.isDebugEnabled())
            log.debug("serverEndpointClassNames {}", Arrays.toString(serverEndpointClassNames));
        for (String serverEndpointClassName : serverEndpointClassNames) {
            Object bean = context.getBean(serverEndpointClassName);
            String[] paths = AnnotationUtils.findAnnotation(bean.getClass(), ServerEndpoint.class).path();
            for (String path : paths) {
                if (this.pathServerEndpointMapping.containsServerEndpointMethodMapping(path)) {
                    throw new WebScoketExcpetion(
                            ExceptionCode.HAVE_A_SAME_ENDPOINTS_ERROR, "websocket端点路径[" + path + "]重复"
                    );
                } else {
                    this.pathServerEndpointMapping.addServerEndpointMapping(
                            path, getServerEndpointMapping(bean)

                    );
                }
            }


        }
    }

    /**
     * 获取端点处理后的实体 {@link  ServerEndpointMethodMapping}。
     *
     * @param bean
     * @return
     * @throws WebScoketExcpetion
     */
    protected ServerEndpointMethodMapping getServerEndpointMapping(Object bean) throws WebScoketExcpetion {
        return ServerEndpointMethodMapping.getInstance(bean);
    }

    /**
     * 扫描{@link ServerEndpoint}的类
     */
    private void scannerPackages() {
        Set<String> packages = scannerWebSocketEndpoints();
        if (packages.isEmpty()) {
            packages = scannerAppliedPackages();
        }
        if (packages.isEmpty()) return;
        if (log.isDebugEnabled())
            log.debug("WebSocketEndpoint scanner packages {}", packages);
        ClassPathBeanDefinitionScanner scanner = getScanner();
        scanner.scan(packages.toArray(new String[packages.size()]));
    }

    /**
     * 扫描{@link WebSocketEndpointScan}注解所在的包
     *
     * @return
     */
    private Set<String> scannerWebSocketEndpoints() {
        Set<String> packages = new HashSet<String>();
        String[] enableWebScoket = context.getBeanNamesForAnnotation(WebSocketEndpointScan.class);
        if (log.isDebugEnabled())
            log.info("EnableWebScoket {}", Arrays.toString(enableWebScoket));
        for (String beanName : enableWebScoket) {
            Object bean = context.getBean(beanName);
            WebSocketEndpointScan enableWebScoketAnnotation =
                    AnnotationUtils.findAnnotation(bean.getClass(), WebSocketEndpointScan.class);
            if (enableWebScoketAnnotation.scanBasePackages().length > 0) {
                for (String basePackage : enableWebScoketAnnotation.scanBasePackages()) {
                    packages.add(basePackage.trim());
                }
            } else if (enableWebScoketAnnotation.scanBaseClasses().length > 0) {
                for (Class baseClass : enableWebScoketAnnotation.scanBaseClasses()) {
                    String basePackage = ClassUtils.getPackageName(baseClass);
                    packages.add(basePackage);
                }
            } else {
                String basePackage = ClassUtils.getPackageName(bean.getClass());
                packages.add(basePackage);
            }
        }
        return packages;
    }

    /**
     * 扫描{@link SpringBootApplication}注解所在的包
     *
     * @return
     */
    private Set<String> scannerAppliedPackages() {
        Set<String> packages = new HashSet<String>();
        String[] enableWebScoket = context.getBeanNamesForAnnotation(SpringBootApplication.class);
        if (log.isDebugEnabled())
            log.info("EnableWebScoket {}", Arrays.toString(enableWebScoket));
        for (String beanName : enableWebScoket) {
            Object bean = context.getBean(beanName);
            SpringBootApplication enableWebScoketAnnotation =
                    AnnotationUtils.findAnnotation(bean.getClass(), SpringBootApplication.class);
            if (enableWebScoketAnnotation.scanBasePackages().length > 0) {
                for (String basePackage : enableWebScoketAnnotation.scanBasePackages()) {
                    packages.add(basePackage.trim());
                }
            } else if (enableWebScoketAnnotation.scanBasePackageClasses().length > 0) {
                for (Class baseClass : enableWebScoketAnnotation.scanBasePackageClasses()) {
                    String basePackage = ClassUtils.getPackageName(baseClass);
                    packages.add(basePackage);
                }
            } else {
                String basePackage = ClassUtils.getPackageName(bean.getClass());
                packages.add(basePackage);
            }
        }
        return packages;
    }

    /**
     * 获取{@link ServerEndpoint}类扫描器
     *
     * @return
     */
    private ClassPathBeanDefinitionScanner getScanner() {
        BeanDefinitionRegistry registry = null;
        if (this.context instanceof BeanDefinitionRegistry) {
            registry = (BeanDefinitionRegistry) this.context;
        }
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ServerEndpoint.class));
        if (this.resourceLoader != null)
            scanner.setResourceLoader(this.resourceLoader);
        return scanner;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
