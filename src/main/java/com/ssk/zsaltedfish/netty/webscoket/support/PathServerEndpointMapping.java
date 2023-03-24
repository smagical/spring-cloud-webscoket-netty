package com.ssk.zsaltedfish.netty.webscoket.support;

import com.ssk.zsaltedfish.netty.webscoket.constant.ExceptionCode;
import com.ssk.zsaltedfish.netty.webscoket.exception.WebScoketExcpetion;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class PathServerEndpointMapping {
    private HashMap<String, ServerEndpointMethodMapping> mappings;
    private volatile ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    public PathServerEndpointMapping() {
        this.mappings = new HashMap<String, ServerEndpointMethodMapping>();
    }

    public ServerEndpointMethodMapping addServerEndpointMapping(
            String path, ServerEndpointMethodMapping serverEndpointMethodMapping) {
        lock.writeLock().lock();
        try {
            return this.mappings.put(path, serverEndpointMethodMapping);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ServerEndpointMethodMappingAndPath getServerEndpointMappingAndPath(String path) throws WebScoketExcpetion {
        lock.readLock().lock();
        try {
            int index = path.indexOf("?");
            if (index != -1) {
                path = path.substring(0, index);
            }
            List<ServerEndpointMethodMappingAndPath> list = new ArrayList<ServerEndpointMethodMappingAndPath>();
            for (Map.Entry<String, ServerEndpointMethodMapping> entry : this.mappings.entrySet()) {
                if (antPathMatcher.match(entry.getKey(), path.trim())) {
                    list.add(new ServerEndpointMethodMappingAndPath(entry.getValue(), entry.getKey()));
                }

            }
            if (list.size() == 1) {
                return list.get(0);
            } else if (list.size() == 0) {
                throw new WebScoketExcpetion(ExceptionCode.NOT_FOUND_SERVERENDPOINT_ERROR,
                        "找不到" + path + "匹配的端点");
            } else {
                throw new WebScoketExcpetion(ExceptionCode.HAVE_TOO_MANYMATCH__SERVERENDPOINT_ERROR,
                        "找到太多" + path + "匹配的端点 ,]" + list.stream().map(
                                e -> e.getClass().getSimpleName()
                        ).collect(Collectors.toList()));
            }
        } finally {
            lock.readLock().unlock();
        }

    }

    public ServerEndpointMethodMapping getServerEndpointMapping(String path) throws WebScoketExcpetion {
        return getServerEndpointMappingAndPath(path).getMapping();
    }

    public boolean containsServerEndpointMethodMapping(String path) {
        lock.readLock().lock();
        try {
            return this.mappings.containsKey(path);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static class ServerEndpointMethodMappingAndPath {
        ServerEndpointMethodMapping mapping;
        String path;

        public ServerEndpointMethodMappingAndPath(ServerEndpointMethodMapping mapping, String path) {
            this.mapping = mapping;
            this.path = path;
        }

        public ServerEndpointMethodMapping getMapping() {
            return mapping;
        }

        public void setMapping(ServerEndpointMethodMapping mapping) {
            this.mapping = mapping;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
