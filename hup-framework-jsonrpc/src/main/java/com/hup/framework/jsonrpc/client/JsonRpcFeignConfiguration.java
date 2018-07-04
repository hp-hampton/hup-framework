package com.hup.framework.jsonrpc.client;

import feign.Feign;
import feign.Retryer;
import org.springframework.context.annotation.Bean;

/**
 * Json Rpc 配置
 *
 * @version 1.0
 */
public class JsonRpcFeignConfiguration {

    @Bean
    public JsonRpcFeignContract feignContract() {
        return new JsonRpcFeignContract();
    }

    @Bean
    public Feign.Builder feignBuilder(Retryer retryer) {
        return Feign.builder().retryer(retryer).requestInterceptor(new JsonRpcFeignClientInterceptor());
    }

}