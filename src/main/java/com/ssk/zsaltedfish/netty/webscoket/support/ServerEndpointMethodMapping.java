package com.ssk.zsaltedfish.netty.webscoket.support;

import com.ssk.zsaltedfish.netty.webscoket.annotation.ServerEndpoint;
import com.ssk.zsaltedfish.netty.webscoket.annotation.invoke.*;
import com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.pojo.AnnatationMethodParam;
import com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove.WebSocketMethodParamReslove;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode.HAVE_TOO_MANY_METHODS_ERROR;

public class ServerEndpointMethodMapping {
    private static ParameterNameDiscoverer parameterNameDiscoverer;
    private List<Class<?>> annotationClasses;
    private Object bean;
    private Map<Class<?>, AnnatationMethodParam> annatationMethodParamHashMap;

    private ServerEndpointMethodMapping(Object bean) throws WebScoketExcpetion {
        this(bean, Arrays.asList(OnOpen.class, OnMessage.class, OnClose.class, OnError.class, BeforeHandShake.class));
    }

    private ServerEndpointMethodMapping(Object bean, List<Class<?>> annotationClasses) throws WebScoketExcpetion {
        parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        ServerEndpoint serverEndpoint = AnnotationUtils.findAnnotation(bean.getClass(), ServerEndpoint.class);
        if (serverEndpoint == null) {
            throw new WebScoketExcpetion(ExceptionCode.NOT_FOUND_SERVERENDPOINT_ERROR, "" +
                    "找不到" + ServerEndpoint.class.getName());
        }
        this.annotationClasses = annotationClasses;

        this.bean = bean;
        if (AopUtils.isAopProxy(bean)) {
            this.bean = AopUtils.getTargetClass(bean);
        }
        HashMap<Class<?>, Method> methodMapping = solveMethod();
        HashMap<Method, List<MethodParameter>> methodParameterMapping = solveMethodParameter(methodMapping);
        this.annatationMethodParamHashMap = new HashMap<Class<?>, AnnatationMethodParam>();
        for (Map.Entry<Class<?>, Method> methodEntry : methodMapping.entrySet()) {
            this.annatationMethodParamHashMap
                    .put(
                            methodEntry.getKey(),
                            AnnatationMethodParam.builder()
                                    .method(methodEntry.getValue())
                                    .methodParameterAndReslove(
                                            getMethodParameterAndReslove(
                                                    methodParameterMapping.getOrDefault(methodEntry.getValue(),
                                                            Collections.emptyList())
                                            )
                                    )
                                    .build()
                    );
        }
    }

    public static ServerEndpointMethodMapping getInstance(Object clazz) throws WebScoketExcpetion {
        return new ServerEndpointMethodMapping(clazz);
    }

    private HashMap<MethodParameter, WebSocketMethodParamReslove> getMethodParameterAndReslove(
            List<MethodParameter> methodParameterList) throws WebScoketExcpetion {

        HashMap<MethodParameter, WebSocketMethodParamReslove> map =
                new HashMap<MethodParameter, WebSocketMethodParamReslove>();
        for (MethodParameter methodParameter : methodParameterList) {
            map.put(methodParameter, WebSocketMethodParamResloveCollection.getParameterResoves(methodParameter));
        }
        return map;
    }

    /**
     * 处理方法所对应的参数
     *
     * @param methodMapping
     *
     * @return
     */
    private HashMap<Method, List<MethodParameter>> solveMethodParameter(HashMap<Class<?>, Method> methodMapping) {
        HashMap<Method, List<MethodParameter>> methodParameterMapping =
                new HashMap<Method, List<MethodParameter>>();
        for (Map.Entry<Class<?>, Method> methodEntry : methodMapping.entrySet()) {
            List<MethodParameter> parameters = new ArrayList<MethodParameter>();
            Method method = methodEntry.getValue();
            for (int i = 0; i < method.getParameterCount(); i++) {
                MethodParameter methodParameter = new MethodParameter(method, i);
                methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
                parameters.add(methodParameter);
            }
            methodParameterMapping.put(method, parameters);
        }
        return methodParameterMapping;
    }

    /**
     * 处理包含webscoket({@link OnOpen} {@link OnMessage} {@link OnClose} {@link OnError})注解的方法
     *
     * @return k-v 类注释-方法
     * @throws WebScoketExcpetion
     */
    private HashMap<Class<?>, Method> solveMethod() throws WebScoketExcpetion {
        HashMap<Class<?>, Method> methodMapping = new HashMap<Class<?>, Method>();
        HashMap<Method, Class<?>> methodAttribution = new HashMap<Method, Class<?>>();
        Class<?> clazz = bean.getClass();
        while (!clazz.getName().equalsIgnoreCase(Object.class.getName())) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    for (Class<?> annotationClass : annotationClasses) {
                        if (annotationClass.isAssignableFrom(annotation.getClass())) {
                            if (!methodMapping.containsKey(annotationClass)) {
                                methodMapping.put(annotationClass, method);
                                methodAttribution.put(method, clazz);
                            } else {
                                Method oldMethod = methodMapping.get(annotationClass);
                                Class<?> oldClass = methodAttribution.get(oldMethod);
                                if (!verifyMethod(oldClass, oldMethod, clazz, method)) {
                                    throw new WebScoketExcpetion(HAVE_TOO_MANY_METHODS_ERROR, clazz.getName()
                                            + "有两个包含" + annotationClass.getName() + "注解的方法");
                                }
                            }
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methodMapping;
    }

    /**
     * 验证包含注解的方法是否重复和是否为重写
     *
     * @param oldClass
     * @param oldMethod
     * @param newClass
     * @param newMethod
     *
     * @return
     */
    private boolean verifyMethod(Class<?> oldClass, Method oldMethod, Class<?> newClass, Method newMethod) {
        if (oldClass.equals(newClass)) return oldMethod.equals(newMethod);
        return oldMethod.getName().equals(newMethod.getName()) &&
                oldMethod.getReturnType().equals(newMethod.getReturnType()) &&
                Arrays.equals(oldMethod.getParameterTypes(), newMethod.getParameterTypes());
    }

    public List<Class<?>> getAnnotationClasses() {
        return annotationClasses;
    }

    public void setAnnotationClasses(List<Class<?>> annotationClasses) {
        this.annotationClasses = annotationClasses;
    }

    public AnnatationMethodParam getMethodParameterAndReslove(Class<?> clazz) {
        return this.annatationMethodParamHashMap.get(clazz);
    }

    public Object getBean() {
        return this.bean;

    }
}
