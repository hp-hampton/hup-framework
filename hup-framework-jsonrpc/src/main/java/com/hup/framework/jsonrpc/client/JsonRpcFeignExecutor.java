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
    //RemoteInvocation invocation 来自JsonRpcProxyFactoryBean
    public RemoteInvocationResult executeRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation) throws Exception {
        //获取请求的类
        Class serviceInterface = ((HttpInvokerClientInterceptor) config).getServiceInterface();
        //获取请求的接口中是否有这个请求方法
        Method serviceMethod = ReflectionUtils.findMethod(serviceInterface, invocation.getMethodName(), invocation.getParameterTypes());
        String requestPath = config.getServiceUrl() + JsonRpcUtil.generateServiceUrl(serviceMethod);
        //获取请求参数
        byte[] requestBytes = (byte[]) invocation.getArguments()[0];
        log.info("jsonrpc remote request url:{}, parmeters:{}", requestPath, new String(requestBytes));
        //获取响应字节
        byte[] responseBytes = getJsonRpcFeignClient().execute(requestPath, requestBytes);
        //将获取的字节存入RemoteInvocationResult，返回给上层
        RemoteInvocationResult invocationResult = new RemoteInvocationResult();
        invocationResult.setValue(responseBytes);
        return invocationResult;
    }

    private JsonRpcFeignClient getJsonRpcFeignClient() {
        return beanFactory.getBean(jsonRpcFeignClientClass);
    }

}
