package com.hup.framework.jsonrpc.client;

import feign.Contract;
import feign.MethodMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class JsonRpcFeignContract extends Contract.BaseContract {

    protected feign.MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        feign.MethodMetadata methodMetadata = super.parseAndValidateMetadata(targetType, method);
        //
        processAnnotationOnMethod(methodMetadata, null, method);
        //
        return methodMetadata;
    }

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        data.template().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        data.template().method(HttpMethod.POST.name());
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation annotation, Method method) {
        //
        data.urlIndex(0);
        data.bodyIndex(1);
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        return true;
    }

}
