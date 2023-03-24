package com.ssk.zsaltedfish.netty.webscoket.support;

import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove.WebSocketMethodParamReslove;
import org.springframework.core.MethodParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode.NOT_FOUND_METHOD_PARAMETER_RESOVE_ERROR;

/**
 * 所有的参数处理器
 */
public class WebSocketMethodParamResloveCollection {


    private static HashSet<WebSocketMethodParamReslove> webSocketMethodParamResloves
            = new HashSet<WebSocketMethodParamReslove>();
    private volatile static AtomicBoolean operate = new AtomicBoolean(false);
    private static List<WebSocketMethodParamReslove> webSocketList = Collections.emptyList();
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * 添加参数处理器
     *
     * @param webSocketMethodParamReslove
     *
     * @return
     */
    public static boolean addMethodParameterAndReslove(
            WebSocketMethodParamReslove webSocketMethodParamReslove) {
        lock.lock();
        try {
            operate.compareAndSet(false, true);
            return WebSocketMethodParamResloveCollection.webSocketMethodParamResloves.add(webSocketMethodParamReslove);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取参数处理器
     *
     * @param methodParameter
     *
     * @return
     */
    public static WebSocketMethodParamReslove getParameterResoves(MethodParameter methodParameter) throws WebScoketExcpetion {
        if (operate.compareAndSet(true, false)) {
            lock.lock();
            try {
                ArrayList<WebSocketMethodParamReslove> list
                        = new ArrayList<WebSocketMethodParamReslove>();
                for (WebSocketMethodParamReslove webSocketMethodParamReslove : webSocketMethodParamResloves) {
                    list.add(webSocketMethodParamReslove);
                }
                Collections.sort(list);
                webSocketList = Collections.unmodifiableList(list);
            } finally {
                lock.unlock();
            }
        }

        for (WebSocketMethodParamReslove webSocketMethodParamReslove : webSocketList) {
            if (webSocketMethodParamReslove.support(methodParameter))
                return webSocketMethodParamReslove;
        }
        throw new WebScoketExcpetion(NOT_FOUND_METHOD_PARAMETER_RESOVE_ERROR,
                methodParameter.getMethod().getName() + "." + methodParameter.getParameterType().getSimpleName() + "找不到适合的参数处理器");
    }

    /**
     * 删除参数处理器
     *
     * @param webSocketMethodParamReslove
     *
     * @return
     */
    public boolean removeMethodParameterAndReslove(
            WebSocketMethodParamReslove webSocketMethodParamReslove) {
        lock.lock();
        try {
            operate.compareAndSet(false, true);
            return WebSocketMethodParamResloveCollection.webSocketMethodParamResloves.remove(webSocketMethodParamReslove);
        } finally {
            lock.unlock();
        }
    }
}
