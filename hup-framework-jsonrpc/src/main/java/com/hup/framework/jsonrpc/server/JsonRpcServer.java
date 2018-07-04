package com.hup.framework.jsonrpc.server;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Json Rpc 服务端注解
 *
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JsonRpcServerRegistrar.class)
public @interface JsonRpcServer {

    String basePath() default "/json-rpc";

    String[] servicePackages() default {};

    Class[] serviceInterfaces() default {};

}
