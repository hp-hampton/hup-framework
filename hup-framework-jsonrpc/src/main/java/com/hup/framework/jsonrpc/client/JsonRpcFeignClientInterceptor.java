package com.hup.framework.jsonrpc.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Json Rpc 客户端拦截器
 *
 * @version 1.0
 */
public class JsonRpcFeignClientInterceptor implements RequestInterceptor {

    private static final ThreadLocal<Map<String, String>> headerLocal = ThreadLocal.withInitial(() -> new HashMap());

    @Override
    public void apply(RequestTemplate template) {
        for (Map.Entry<String, String> entry : headerLocal.get().entrySet()) {
            template.header(entry.getKey(), entry.getValue());
        }
    }

    public static void header(String name, String value) {
        headerLocal.get().put(name, value);
    }

    public static void clear() {
        headerLocal.remove();
    }

}
