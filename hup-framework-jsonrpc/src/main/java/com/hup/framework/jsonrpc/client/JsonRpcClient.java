package com.hup.framework.jsonrpc.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Json Rpc 客户端注解
 *
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcClient {

    String basePath() default "/json-rpc";

    String[] servicePackages() default {};

    Class[] serviceInterfaces() default {};

}
