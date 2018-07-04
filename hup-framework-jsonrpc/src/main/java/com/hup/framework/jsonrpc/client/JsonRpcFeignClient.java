package com.hup.framework.jsonrpc.client;

/**
 * Json Rpc Feign Client
 *
 * @version 1.0
 */
public interface JsonRpcFeignClient {

    byte[] execute(String requestPath, byte[] requestBody);

}
