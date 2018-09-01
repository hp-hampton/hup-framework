package com.hup.framework.advice.Interceptor;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class OpenApiInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<Boolean> openApiLocal = ThreadLocal.withInitial(() -> false);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //
        Api api = findApiAnnotation(handler);
        //
        if (api != null) {
            openApiLocal.set(Boolean.TRUE);
            //
            log.debug("Handler {} is an api operation.", handler.toString());
        }
        //
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 恢复初始值设置
        openApiLocal.set(Boolean.FALSE);
    }

    public static boolean isOpenApi() {
        return openApiLocal.get();
    }

    private Api findApiAnnotation(Object handler) {
        Api api = AnnotationUtils.findAnnotation(handler.getClass(), Api.class);
        if (api == null && handler instanceof HandlerMethod) {
            api = AnnotationUtils.findAnnotation(((HandlerMethod) handler).getBeanType(), Api.class);
        }
        return api;
    }
}
