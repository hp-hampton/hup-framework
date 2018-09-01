package com.hup.framework.advice.method;


import com.hup.framework.advice.ApiResponseBody;
import com.hup.framework.advice.Interceptor.OpenApiInterceptor;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 控制器增强器,将控制器的返回结果统一格式返回
 */
@RestControllerAdvice
public class DefinitionResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return OpenApiInterceptor.isOpenApi();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (skipBody(body)) {
            return body;
        }
        if (body == null) {
            return ApiResponseBody.builder().success(true).build();
        }
        if (!(body instanceof ApiResponseBody)) {
            return ApiResponseBody.builder().success(true).data(body).build();
        }
        return body;
    }

    private boolean skipBody(Object body) {
        if (body instanceof InputStreamResource) {
            return true;
        }
        return false;
    }
}
