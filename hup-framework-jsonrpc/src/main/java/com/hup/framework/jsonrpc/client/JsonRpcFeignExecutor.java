package com.hup.framework.jsonrpc.client;

import com.hup.framework.jsonrpc.JsonRpcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerClientInterceptor;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Slf4j
public class JsonRpcFeignExecutor implements HttpInvokerRequestExecutor, BeanFactoryAware {

    private BeanFactory beanFactory;

    private Class<? extends JsonRpcFeignClient> jsonRpcFeignClientClass;

    public void setJsonRpcFeignClientClass(Class<? extends JsonRpcFeignClient> jsonRpcFeignClientClass) {
        this.jsonRpcFeignClientClass = jsonRpcFeignClientClass;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public RemoteInvocationResult executeRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation) throws Exception {
        //
        Class serviceInterface = ((HttpInvokerClientInterceptor) config).getServiceInterface();
        Method serviceMethod = ReflectionUtils.findMethod(serviceInterface, invocation.getMethodName(), invocation.getParameterTypes());
        String requestPath = config.getServiceUrl() + JsonRpcUtil.generateServiceUrl(serviceMethod);
        //
        byte[] requestBytes = (byte[]) invocation.getArguments()[0];
        log.info("jsonrpc remote request url:{}, parmeters:{}", requestPath, new String(requestBytes));
        byte[] responseBytes = getJsonRpcFeignClient().execute(requestPath, requestBytes);
        //
        RemoteInvocationResult invocationResult = new RemoteInvocationResult();
        invocationResult.setValue(responseBytes);
        //
        return invocationResult;
    }

    private JsonRpcFeignClient getJsonRpcFeignClient() {
        return beanFactory.getBean(jsonRpcFeignClientClass);
    }

}
