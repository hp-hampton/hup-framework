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
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
        // 初始化 RPC 上下文
        JsonRpcContext.init(serviceMethod);
        JsonRpcContext.get().putObject(HttpServletRequest.class.getName(), request);
        JsonRpcRequest jsonRcpRequest = getObjectMapper().readValue(is, JsonRpcRequest.class);
        //
        request.setAttribute(JsonRpcRequest.class.getName(), jsonRcpRequest);
        //
        RemoteInvocation remoteInvocation = new RemoteInvocation();
        remoteInvocation.setMethodName(serviceMethod.getName());
        remoteInvocation.setArguments(jsonRcpRequest.getParams());
        remoteInvocation.setParameterTypes(serviceMethod.getParameterTypes());
        //
        return remoteInvocation;
    }

    @Override
    protected void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result) throws IOException {
        response.setContentType(getContentType());
        //
        JsonRpcRequest jsonRcpRequest = (JsonRpcRequest) request.getAttribute(JsonRpcRequest.class.getName());
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
        //
        getObjectMapper().writeValue(response.getOutputStream(), jsonRcpResponse);
        // 销毁 RPC 上下文
        JsonRpcContext.clear();
    }

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void afterPropertiesSet() {
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
