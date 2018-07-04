package com.hup.framework.jsonrpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JsonRpcContext {

    private Method method;

    private Map<String, Object> context = new HashMap<>();

    private JsonRpcContext(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Object getObject(String name) {
        return context.get(name);
    }

    public void putObject(String name, Object value) {
        this.context.put(name, value);
    }

    private static ThreadLocal<JsonRpcContext> contextLocal = new ThreadLocal<>();

    public static void init(Method method) {
        JsonRpcContext.contextLocal.set(new JsonRpcContext(method));
    }

    public static void clear() {
        JsonRpcContext.contextLocal.remove();
    }

    public static JsonRpcContext get() {
        return JsonRpcContext.contextLocal.get();
    }
}
