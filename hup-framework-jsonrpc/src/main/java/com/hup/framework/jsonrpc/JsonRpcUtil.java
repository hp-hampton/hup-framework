package com.hup.framework.jsonrpc;


import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JsonRpcUtil {

    public static String generateServiceUrl(Method method) {
        return new StringBuilder("/")
                .append(method.getDeclaringClass().getName())
                .append(".").append(method.getName())
                .append("(")
                .append(generateParameterDesc(method.getParameterTypes()))
                .append(")")
                .toString();
    }

    private static String generateParameterDesc(Class[] parameterTypes) {
        List<String> parameterNames = new ArrayList<>();
        for(Class parameterType : parameterTypes) {
            parameterNames.add(parameterType.getName());
        }
        //
        return StringUtils.collectionToCommaDelimitedString(parameterNames);
    }

}
