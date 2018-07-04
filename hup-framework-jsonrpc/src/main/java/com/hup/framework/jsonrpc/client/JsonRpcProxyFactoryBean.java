package com.hup.framework.jsonrpc.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hup.framework.jsonrpc.JsonRpcContext;
import com.hup.framework.jsonrpc.JsonRpcRequest;
import com.hup.framework.jsonrpc.JsonRpcResponse;
import com.hup.framework.jsonrpc.serialization.JsonRpcResponseResultDeserializer;
import com.hup.framework.support.exception.DefinitionBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Json Rpc 代理
 *
 * @version 1.0
 */
@Slf4j
public class JsonRpcProxyFactoryBean extends HttpInvokerProxyFactoryBean {

    private ObjectMapper objectMapper;

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        //
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Object.class, new JsonRpcResponseResultDeserializer());
        //
        this.objectMapper = new ObjectMapper()
                .registerModule(simpleModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws Exception {
        //组装jsonrpc2.0 规范----请求参数
        JsonRpcRequest jsonRcpRequest = new JsonRpcRequest();
        jsonRcpRequest.setJsonrcp("2.0");
        jsonRcpRequest.setMethod(invocation.getMethodName());
        jsonRcpRequest.setParams(invocation.getArguments());
        //
        log.info("JsonRpcClient 调用请求 {}", jsonRcpRequest);
        //序列化请求参数
        byte[] requestBytes = getObjectMapper().writeValueAsBytes(jsonRcpRequest);
        //
        invocation.setArguments(new Object[]{requestBytes});
        //请求调用远程方法，获取返回结果
        RemoteInvocationResult result = super.executeRequest(invocation);
        //
        byte[] responseBytes = (byte[]) result.getValue();
        //
        Method method = ReflectionUtils.findMethod(getServiceInterface(), invocation.getMethodName(), invocation.getParameterTypes());
        //json反序列Response，在readValue中使用，若不加serialization里面的正反序列化类在获取出来的结果是map类型
        JsonRpcContext.init(method);
        JsonRpcResponse jsonRpcResponse = getObjectMapper().readValue(responseBytes, JsonRpcResponse.class);
        JsonRpcContext.clear();
        //
        log.info("JsonRpcClient 调用响应 {}", jsonRpcResponse);
        //
        JsonRpcResponse.Error error = jsonRpcResponse.getError();
        if (error == null) {
            result.setValue(jsonRpcResponse.getResult());
        } else {
            if (error.isPpsException()) {
                result.setException(DefinitionBusinessException.biz(error.getCode()).message(error.getMessage()).build());
            } else {
                result.setException(new RemoteAccessException(error.toString()));
            }
        }
        //
        return result;
    }

}
