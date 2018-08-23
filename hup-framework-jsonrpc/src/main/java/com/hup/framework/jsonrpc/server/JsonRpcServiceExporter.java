package com.hup.framework.jsonrpc.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hup.framework.jsonrpc.JsonRpcContext;
import com.hup.framework.jsonrpc.JsonRpcRequest;
import com.hup.framework.jsonrpc.JsonRpcResponse;
import com.hup.framework.jsonrpc.serialization.JsonRpcRequestParamsDeserializer;
import com.hup.framework.support.exception.DefinitionBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Json Rpc 服务导出者
 *
 * @version 1.0
 */
@Slf4j
public class JsonRpcServiceExporter extends HttpInvokerServiceExporter {

    private ObjectMapper objectMapper;

    private Method serviceMethod;

    public void setServiceMethod(Method serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    @Override
    //从给定的http请求中读取远程调用内容
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
        // 初始化 RPC 上下文
        JsonRpcContext.init(serviceMethod);
        JsonRpcContext.get().putObject(HttpServletRequest.class.getName(), request);
        //进入JsonRpcRequestParamsDeserializer序列化类，将请求参数序列化出来
        JsonRpcRequest jsonRcpRequest = getObjectMapper().readValue(is, JsonRpcRequest.class);
        //将jsonRcpRequest 作为一个属性设置到HttpServletRequest中
        request.setAttribute(JsonRpcRequest.class.getName(), jsonRcpRequest);
        //构造一个远程请求调用的RemoteInvocation
        RemoteInvocation remoteInvocation = new RemoteInvocation();
        remoteInvocation.setMethodName(serviceMethod.getName());
        remoteInvocation.setArguments(jsonRcpRequest.getParams());
        remoteInvocation.setParameterTypes(serviceMethod.getParameterTypes());
        return remoteInvocation;
    }


    /**
     * HttpInvokerServiceExporter类中
     * RemoteInvocation invocation = readRemoteInvocation(request);
     * 反射远程调用给定的url，拿到响应结果
     * RemoteInvocationResult result = invokeAndCreateResult(invocation, getProxy());
     * writeRemoteInvocationResult(request, response, result);
     */
    @Override
    protected void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result) throws IOException {
        //设置内容类型-----application/utf-8
        response.setContentType(getContentType());
        //获取readRemoteInvocation设置的属性
        JsonRpcRequest jsonRcpRequest = (JsonRpcRequest) request.getAttribute(JsonRpcRequest.class.getName());
        //构造返回的响应体
        JsonRpcResponse jsonRcpResponse = new JsonRpcResponse();
        jsonRcpResponse.setId(jsonRcpRequest.getId());
        jsonRcpResponse.setJsonrcp(jsonRcpRequest.getJsonrcp());
        if (result.getException() == null) {
            jsonRcpResponse.setResult(result.getValue());
        } else if (result.getException().getCause() instanceof DefinitionBusinessException) {
            DefinitionBusinessException definitionBusinessException = (DefinitionBusinessException) result.getException().getCause();
            JsonRpcResponse.Error error = new JsonRpcResponse.Error(definitionBusinessException.getCode(), definitionBusinessException.getMessage());
            error.setPpsException(true);
            jsonRcpResponse.setError(error);
        } else {
            log.error("服务调用异常！", result.getException());
            jsonRcpResponse.setError(new JsonRpcResponse.Error("S_1010", "服务调用异常"));
        }
        //将响应体转为输出流
        getObjectMapper().writeValue(response.getOutputStream(), jsonRcpResponse);
        // 销毁 RPC 上下文
        JsonRpcContext.clear();
    }

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void afterPropertiesSet() {
        //在这个父类的方法中会动态代理service的实现类，其主要的目的是加个Proxy的拦截器，方便后续加其他的拦截器
        super.afterPropertiesSet();
        //
        setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        //
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Object[].class, new JsonRpcRequestParamsDeserializer());
        //
        this.objectMapper = new ObjectMapper()
                .registerModule(simpleModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}
