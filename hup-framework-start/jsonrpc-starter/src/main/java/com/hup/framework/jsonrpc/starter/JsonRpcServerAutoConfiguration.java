package com.hup.framework.jsonrpc.starter;

import com.hup.framework.jsonrpc.JsonRpcUtil;
import com.hup.framework.jsonrpc.server.JsonRpcServerRegistrar;
import com.hup.framework.jsonrpc.server.JsonRpcServiceExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

@Slf4j
@Configuration
@ConditionalOnClass(JsonRpcServiceExporter.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class JsonRpcServerAutoConfiguration implements BeanClassLoaderAware, BeanDefinitionRegistryPostProcessor {

    private ClassLoader beanClassLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            String serviceClassName = beanDefinition.getBeanClassName();
            if (serviceClassName == null) {
                continue;
            }
            //根据入参，查看参数是否有继承接口，若继承了接口切接口是自己定义需要暴露的接口则返回
            Class serviceInterface = getJsonRpcServiceInterface(serviceClassName);
            if (serviceInterface == null) {
                continue;
            }
            //获取serviceInterface上所有方法，针对每个方法调用filter实现匹配检查，如果匹配上，调用MethodCallback回调方法。
            // 该方法会递归向上查询所有父类和实现的接口上的所有方法并处理；
            ReflectionUtils.doWithMethods(serviceInterface, method -> {
                //每一个方法都会拥有一个相对应的bean
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcServiceExporter.class)
                        .addPropertyValue("serviceMethod", method)
                        .addPropertyValue("serviceInterface", serviceInterface)
                        .addPropertyValue("service", new RuntimeBeanReference(beanDefinitionName));
                //组装一个请求地址
                String serviceExporterBeanName = JsonRpcServerRegistrar.getBasePath() + JsonRpcUtil.generateServiceUrl(method);
                //注册
                registry.registerBeanDefinition(serviceExporterBeanName, builder.getBeanDefinition());
            }, method -> {
                return method.getDeclaringClass().getName().contains("com.hup");
            });
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    private Class getJsonRpcServiceInterface(String serviceClassName) {
        try {
            Class serviceClass = ClassUtils.forName(serviceClassName, beanClassLoader);
            for (Class serviceClassInterface : serviceClass.getInterfaces()) {
                //自定义需要暴露的包路径下的类
                for (Class serviceInterface : JsonRpcServerRegistrar.getServiceInterfaces()) {
                    if (serviceClassInterface == serviceInterface) {
                        return serviceClassInterface;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
